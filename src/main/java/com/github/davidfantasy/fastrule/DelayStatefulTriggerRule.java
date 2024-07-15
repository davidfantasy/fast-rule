package com.github.davidfantasy.fastrule;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.github.davidfantasy.fastrule.condition.Condition;
import com.github.davidfantasy.fastrule.fact.Fact;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 有状态的规则，会保存每个触发过当前规则的fact，同时支持规则的延迟触发。
 * 相同的采集fact对于同一规则只会触发一次，直到某个fact不再满足该规则条件时，后续的fact才会重新触发该规则
 * 举例：
 * rule1 当 fact a>5 时触发执行
 * 当规则引擎收到 5个采集的a的值时：
 * a=1 不满足，不会执行rule1
 * a=6 满足，执行rule1
 * a=8 满足，但不会执行rule1，因为前面已经触发过了
 * a=4 不满足，不会执行rule1，但会重置规则的执行状态
 * a=7 满足，执行rule1
 * 延迟触发，举例：
 * rule1 当 fact a>5 时触发执行，triggerDelayMS设置5000
 * 当规则引擎收到 4个采集的a的值时：
 * a=1 不满足，不会执行rule1
 * a=6 满足，加入到等待触发队列
 * a=8 满足，但a=6已加入等待触发队列，忽略该值
 * a=7 满足，但a=6已加入等待触发队列，忽略该值
 * 等待5000ms后，以a=6触发rule1的执行
 * 如果在5000ms内：
 * a=4 不满足，删除等待队列中a的值，到期后不会再执行
 **/
@Slf4j
public abstract class DelayStatefulTriggerRule extends BaseRule {

    protected final ConcurrentHashSet<String> triggeredFactIds;

    protected Map<String, ScheduledFuture<?>> delayedFacts;

    private Long triggerDelayMS;

    protected final ReentrantLock lock = new ReentrantLock();

    private static final ScheduledExecutorService delayTriggerExecutor = Executors.newScheduledThreadPool(1,
            new ThreadFactory() {
                private final AtomicInteger index = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("fast-rule-scheduled-delay-trigger-" + index.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            });

    public DelayStatefulTriggerRule(String id, String name, Integer priority, String description, Condition condition, Long triggerDelayMS) {
        super(id, name, priority, description, condition);
        triggeredFactIds = new ConcurrentHashSet<>();
        if (triggerDelayMS != null) {
            if (triggerDelayMS < 1000) {
                throw new IllegalArgumentException("triggerDelayMS must be greater than 1000");
            }
            this.triggerDelayMS = triggerDelayMS;
            this.delayedFacts = new ConcurrentHashMap<>();
        }
    }

    @Override
    public void executeThen(Fact fact) {
        lock.lock();
        try {
            if (triggeredFactIds.contains(fact.getId())) {
                log.debug("fact {} is already triggered rule {},ignore current fact", fact.getId(), this.getName());
                return;
            } else {
                triggeredFactIds.add(fact.getId());
            }
            if (this.triggerDelayMS != null && !delayedFacts.containsKey(fact.getId())) {
                log.debug("current rule {} enabled  delayed triggering, the fact {} has been added to the delay queue.", fact.getId(), this.getName());
                ScheduledFuture<?> future = delayTriggerExecutor.schedule(() -> {
                    doExecuteThen(fact);
                }, triggerDelayMS, TimeUnit.MILLISECONDS);
                delayedFacts.put(fact.getId(), future);
                return;
            }
            doExecuteThen(fact);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void executeElse(Fact fact) {
        lock.lock();
        try {
            if (triggeredFactIds.contains(fact.getId())) {
                triggeredFactIds.remove(fact.getId());
                doExecuteElse(fact);
            }
            if (this.delayedFacts != null) {
                ScheduledFuture<?> future = delayedFacts.get(fact.getId());
                if (future != null) {
                    future.cancel(true);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void enable() {
        super.enable();
    }

    @Override
    public void disable() {
        super.disable();
    }

    protected abstract void doExecuteThen(Fact fact);

    protected abstract void doExecuteElse(Fact fact);

}
