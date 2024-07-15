package com.github.davidfantasy.fastrule.fact.collector;

import com.github.davidfantasy.fastrule.fact.Fact;
import com.github.davidfantasy.fastrule.fact.FactCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 自带缓存队列的fact采集器，支持并发采集
 */
public abstract class CachedQueueCollector implements FactCollector {

    private final LinkedBlockingQueue<Fact> cacheQueue;

    public CachedQueueCollector(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than 0");
        }
        cacheQueue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public List<Fact> collect() {
        List<Fact> results = new ArrayList<>();
        cacheQueue.drainTo(results);
        return results;
    }

    protected void addFact(Fact fact) {
        cacheQueue.offer(fact);
    }

}
