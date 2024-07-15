package com.github.davidfantasy.fastrule.executor;

import com.github.davidfantasy.fastrule.Rule;
import com.github.davidfantasy.fastrule.RuleManager;
import com.github.davidfantasy.fastrule.RulesEngineConfig;
import com.github.davidfantasy.fastrule.fact.Fact;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RuleTaskExecutor {

    private final Disruptor<RuleTaskEvent> disruptor;

    private RingBuffer<RuleTaskEvent> ringBuffer;

    public RuleTaskExecutor(int bufferSize, int numberOfConsumers) {
        RuleTaskEventFactory eventFactory = new RuleTaskEventFactory();

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("fast-rule-executor-" + index.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };
        disruptor = new Disruptor<>(
                eventFactory,
                bufferSize,
                threadFactory);

        WorkHandler<RuleTaskEvent>[] workers = new WorkHandler[numberOfConsumers];
        for (int i = 0; i < numberOfConsumers; i++) {
            workers[i] = new RuleTaskEventHandler();
        }
        //disruptor最终会初始化numberOfConsumers个线程，线程工厂是在构造器中传入的threadFactory
        disruptor.handleEventsWithWorkerPool(workers);
        disruptor.setDefaultExceptionHandler(new RuleTaskExceptionHandler());

    }

    public void shutdown() {
        disruptor.shutdown();
    }

    public void start() {
        ringBuffer = disruptor.start();
    }

    public void submit(Fact fact, Rule rule, RulesEngineConfig rulesEngineConfig) {
        if (ringBuffer == null) {
            throw new IllegalStateException("RuleTaskExecutor is not started");
        }
        long sequence = ringBuffer.next();
        try {
            RuleTaskEvent event = ringBuffer.get(sequence);
            event.setRule(rule);
            event.setFact(fact);
            event.setRulesEngineConfig(rulesEngineConfig);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    public void submit(Fact fact, RuleManager ruleManager, RulesEngineConfig rulesEngineConfig) {
        if (ringBuffer == null) {
            throw new IllegalStateException("RuleTaskExecutor is not started");
        }
        long sequence = ringBuffer.next();
        try {
            RuleTaskEvent event = ringBuffer.get(sequence);
            event.setFact(fact);
            event.setRulesEngineConfig(rulesEngineConfig);
            event.setRuleManager(ruleManager);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

}
