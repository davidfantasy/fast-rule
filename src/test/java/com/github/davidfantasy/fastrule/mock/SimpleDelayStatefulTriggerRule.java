package com.github.davidfantasy.fastrule.mock;

import com.github.davidfantasy.fastrule.DelayStatefulTriggerRule;
import com.github.davidfantasy.fastrule.condition.Condition;
import com.github.davidfantasy.fastrule.fact.Fact;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class SimpleDelayStatefulTriggerRule extends DelayStatefulTriggerRule {


    private final List<Fact> hitFacts = new CopyOnWriteArrayList<>();

    private final List<Fact> missFacts = new CopyOnWriteArrayList<>();


    public SimpleDelayStatefulTriggerRule(String name, Condition condition, Long triggerDelayMS) {
        super(name, name, 1, null, condition, triggerDelayMS);
    }

    @Override
    protected boolean doExecuteThen(Fact fact) {
        hitFacts.add(fact);
        return true;
    }

    @Override
    protected boolean doExecuteElse(Fact fact) {
        missFacts.add(fact);
        return true;
    }

    public void clear() {
        delayedFacts.clear();
        hitFacts.clear();
        missFacts.clear();
        triggered.set(false);
    }

}
