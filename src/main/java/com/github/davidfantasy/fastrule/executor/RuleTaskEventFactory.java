package com.github.davidfantasy.fastrule.executor;

import com.lmax.disruptor.EventFactory;

public class RuleTaskEventFactory implements EventFactory<RuleTaskEvent> {
    @Override
    public RuleTaskEvent newInstance() {
        return new RuleTaskEvent();
    }
}
