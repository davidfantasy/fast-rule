package com.github.davidfantasy.fastrule.mock;


import cn.hutool.core.date.SystemClock;
import com.github.davidfantasy.fastrule.fact.Fact;
import com.github.davidfantasy.fastrule.fact.SimpleFact;
import com.github.davidfantasy.fastrule.fact.collector.CachedQueueCollector;

public class MockManualCollector extends CachedQueueCollector {

    public MockManualCollector() {
        super(1000);
    }

    public synchronized void mockFact(String name, Object value) {
        Fact mockFact = new SimpleFact(name, name, value, SystemClock.now());
        this.addFact(mockFact);
    }


    @Override
    public void startup() {
    }

    @Override
    public void shutdown() {
    }

}
