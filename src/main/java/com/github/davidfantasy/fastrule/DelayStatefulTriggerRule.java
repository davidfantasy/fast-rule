package com.github.davidfantasy.fastrule;

import cn.hutool.core.collection.CollUtil;
import com.github.davidfantasy.fastrule.condition.Condition;
import com.github.davidfantasy.fastrule.fact.Fact;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 有状态的规则，会保存规则的触发状态，触发中的规则不会被再次触发，直到某个fact不再满足规则条件重置规则的触发状态；
 * 举例：
 * rule1 当 fact a>5 时触发执行
 * 当规则引擎收到 5个采集的a的值时：
 * a=1 不满足，不会执行rule1
 * a=6 满足，执行rule1
 * a=8 满足，但不会执行rule1，因为前面已经触发过了
 * a=4 不满足，不会执行rule1，但会重置规则的执行状态
 * a=7 满足，执行rule1
 * 同时支持fact对规则的延迟触发，举例：
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

    protected AtomicBoolean triggered;

    protected Map<String, ScheduledFuture<?>> delayedFacts;

    protected Long triggerDelayMS;

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
        triggered = new AtomicBoolean(false);
        assignTriggerDelayMS(triggerDelayMS);
    }

    protected void assignTriggerDelayMS(Long triggerDelayMS) {
        if (triggerDelayMS != null) {
            if (triggerDelayMS < 1000) {
                throw new IllegalArgumentException("triggerDelayMS must be greater than 1000");
            }
            this.delayedFacts = new ConcurrentHashMap<>();
            this.triggerDelayMS = triggerDelayMS;
        } else {
            this.triggerDelayMS = null;
        }
    }

    public void setTriggeredStatus(boolean triggered) {
        this.triggered.set(triggered);
    }

    public boolean hasTriggered() {
        return this.triggered.get();
    }

    public void mergeState(DelayStatefulTriggerRule other) {
        setTriggeredStatus(other.triggered.get());
        this.delayedFacts = other.delayedFacts;
    }

    public void clearDelayedFacts() {
        if (!CollUtil.isEmpty(delayedFacts)) {
            this.delayedFacts.forEach((k, v) -> v.cancel(true));
            this.delayedFacts.clear();
        }
    }

    @Override
    public void executeThen(Fact fact) {
        lock.lock();
        try {
            if (hasTriggered()) {
                log.debug("rule {} is already triggered, ignore current fact:{}", this.getName(), fact.getId());
                return;
            }
            if (this.triggerDelayMS != null) {
                //如果延迟队列里面已经有该fact了，则忽略
                if (delayedFacts.containsKey(fact.getId())) {
                    log.debug("fact {} is already add to delayed queue:{}，ignored", fact.getId(), this.getName());
                    return;
                }
                log.debug("current rule {} enabled  delayed triggering, the fact {} has been added to the delay queue.", fact.getId(), this.getName());
                ScheduledFuture<?> future = delayTriggerExecutor.schedule(() -> {
                    if (hasTriggered()) {
                        log.debug("rule {} is already triggered, ignore delay fact:{}", this.getName(), fact.getId());
                        return;
                    }
                    if (doExecuteThen(fact)) {
                        setTriggeredStatus(true);
                    }
                    delayedFacts.remove(fact.getId());
                }, triggerDelayMS, TimeUnit.MILLISECONDS);
                delayedFacts.put(fact.getId(), future);
            } else {
                if (doExecuteThen(fact)) {
                    setTriggeredStatus(true);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void executeElse(Fact fact) {
        lock.lock();
        try {
            if (this.delayedFacts != null) {
                ScheduledFuture<?> future = delayedFacts.get(fact.getId());
                if (future != null) {
                    future.cancel(true);
                    delayedFacts.remove(fact.getId());
                }
            }
            if (doExecuteElse(fact)) {
                this.triggered.set(false);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 处理触发逻辑，返回值表示是否需要更新规则的触发状态
     */
    protected abstract boolean doExecuteThen(Fact fact);

    /**
     * 处理恢复逻辑，返回值表示是否需要恢复规则的触发状态
     */
    protected abstract boolean doExecuteElse(Fact fact);

}
