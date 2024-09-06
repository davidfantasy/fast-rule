package com.github.davidfantasy.fastrule;

import cn.hutool.core.lang.Assert;
import com.github.davidfantasy.fastrule.executor.RuleTaskExecutor;
import com.github.davidfantasy.fastrule.fact.Fact;
import com.github.davidfantasy.fastrule.fact.FactCollector;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class DefaultRuleEngine implements RuleEngine {

    private final RuleManager ruleManager;

    private final RuleTaskExecutor ruleTaskExecutor;

    private final RulesEngineConfig rulesEngineConfig;

    private final ScheduledExecutorService collectorExecutor;

    private List<FactCollectorInfo> factCollectors = new ArrayList<>();

    private AtomicBoolean started = new AtomicBoolean(false);

    public DefaultRuleEngine(RuleManager ruleManager, RulesEngineConfig rulesEngineConfig) {
        this.ruleManager = ruleManager;
        if (rulesEngineConfig != null) {
            this.rulesEngineConfig = rulesEngineConfig;
        } else {
            this.rulesEngineConfig = RulesEngineConfig.builder().build();
        }
        Assert.notNull(ruleManager, "ruleManager must not be null");
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        ruleTaskExecutor = new RuleTaskExecutor(4096, corePoolSize * 2);
        collectorExecutor = Executors.newScheduledThreadPool(this.rulesEngineConfig.getCollectorScheduledThreadPoolSize(),
                new ThreadFactory() {
                    private final AtomicInteger index = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("fast-rule-scheduled-collector-" + index.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    }
                });
    }

    @Override
    public RuleManager getRuleManager() {
        return ruleManager;
    }

    @Override
    public RulesEngineConfig getConfig() {
        return this.rulesEngineConfig;
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            ruleTaskExecutor.start();
            startCollectors();
        } else {
            log.warn("rule engine already started");
        }
    }

    @Override
    public void shutdown() {
        if (started.compareAndSet(true, false)) {
            ruleTaskExecutor.shutdown();
            stopCollectors();
        }
    }

    /**
     * 如果无需保障规则顺序，默认会将fact和rule分散到不同的事件队列中去异步执行，这样性能更高。
     * 如果firedByPriority为true，则单个fact会确保会按照优先级顺序同步的比对所有的规则，并根据RulesEngineConfig
     * 中的设置进行规则跳过。
     */
    @Override
    public void fire(Fact fact, boolean firedByPriority) {
        if (!started.get()) {
            throw new IllegalStateException("rule engine not started");
        }
        if (firedByPriority) {
            ruleTaskExecutor.submit(fact, ruleManager, this.rulesEngineConfig);
        } else {
            ruleManager.forEach(rule -> {
                if (rule.isEnabled()) {
                    ruleTaskExecutor.submit(fact, rule, null);
                }
                return true;
            });
        }
    }

    @Override
    public void fireByCollector(long collectIntervalMs, FactCollector collector, boolean firedByPriority) {
        if (!started.get()) {
            throw new IllegalStateException("rule engine not started");
        }
        boolean dup = factCollectors.stream().anyMatch(f -> f.getFactCollector().getName().equals(collector.getName()));
        if (dup) {
            throw new IllegalArgumentException("重复注册collector：" + collector.getClass().toString());
        }
        collector.startup();
        ScheduledFuture<?> future = collectorExecutor.scheduleWithFixedDelay(() -> {
            try {
                collector.collect().forEach(fact -> {
                    fire(fact, firedByPriority);
                });
            } catch (Exception e) {
                log.warn("rule fact collect error: {},{}", collector.getName(), e.getMessage());
            }
        }, collectIntervalMs, collectIntervalMs, TimeUnit.MILLISECONDS);
        this.factCollectors.add(new FactCollectorInfo(collector, collectIntervalMs, firedByPriority, future));
    }

    @Override
    public void removeFactCollector(String name) {
        this.factCollectors = factCollectors.stream().filter(data -> {
            FactCollector collector = data.getFactCollector();
            if (collector.getName().equals(name)) {
                if (data.getFuture() != null) {
                    data.future.cancel(true);
                    collector.shutdown();
                }
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }


    private void stopCollectors() {
        for (FactCollectorInfo f : factCollectors) {
            if (f.getFuture() != null && !f.getFuture().isDone()) {
                f.getFuture().cancel(true);
                f.getFactCollector().shutdown();
            }
        }
    }

    private void startCollectors() {
        for (FactCollectorInfo f : factCollectors) {
            if (f.getFuture() != null && !f.getFuture().isDone()) {
                continue;
            }
            FactCollector collector = f.getFactCollector();
            collector.startup();
            ScheduledFuture<?> newFuture = collectorExecutor.scheduleWithFixedDelay(() -> {
                try {
                    List<Fact> facts = collector.collect();
                    for (Fact fact : facts) {
                        fire(fact, f.isFiredByPriority());
                    }
                } catch (Exception e) {
                    log.warn("rule fact collect error: {}", e.getMessage());
                }
            }, f.getCollectIntervalMs(), f.getCollectIntervalMs(), TimeUnit.MILLISECONDS);
            f.setFuture(newFuture);
            log.info("已启动规则fact采集器：{}", f.getClass().getName());
        }
    }


    @Data
    @AllArgsConstructor
    static class FactCollectorInfo {

        private FactCollector factCollector;

        private long collectIntervalMs;

        private boolean firedByPriority;

        private ScheduledFuture<?> future;

    }


}
