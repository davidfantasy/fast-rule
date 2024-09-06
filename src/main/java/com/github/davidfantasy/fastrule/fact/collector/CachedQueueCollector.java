package com.github.davidfantasy.fastrule.fact.collector;

import com.github.davidfantasy.fastrule.fact.Fact;
import com.github.davidfantasy.fastrule.fact.FactCollector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 自带缓存队列的fact采集器，支持并发采集
 */
@Slf4j
public class CachedQueueCollector implements FactCollector {

    private final LinkedBlockingQueue<Fact> cacheQueue;

    public CachedQueueCollector(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than 0");
        }
        cacheQueue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public List<Fact> collect() {
        var results = new ArrayList<Fact>();
        try {
            //如果当前没有数据则阻塞collect，直到有数据产生,避免空轮询
            results.add(cacheQueue.take());
            //将剩下的数据放入结果集
            cacheQueue.drainTo(results);
        } catch (InterruptedException e) {
            log.error("Thread was interrupted while waiting for facts", e);
        }
        return results;
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {
        cacheQueue.clear();
    }

    protected void addFact(Fact fact) {
        if (!cacheQueue.offer(fact)) {
            log.warn("cache queue is full,fact will be dropped:{}", fact.getId());
        }
    }

}
