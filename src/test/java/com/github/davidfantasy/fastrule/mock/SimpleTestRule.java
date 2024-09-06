package com.github.davidfantasy.fastrule.mock;

import com.github.davidfantasy.fastrule.BaseRule;
import com.github.davidfantasy.fastrule.condition.Condition;
import com.github.davidfantasy.fastrule.fact.Fact;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class SimpleTestRule extends BaseRule {


    private final List<Fact> hitFacts = new CopyOnWriteArrayList<>();

    private final List<Fact> missFacts = new CopyOnWriteArrayList<>();


    public SimpleTestRule(String id, Integer priority, Condition condition) {
        super(id, id, priority, null, condition);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void executeThen(Fact fact) {
        hitFacts.add(fact);
    }

    @Override
    public void executeElse(Fact fact) {
        missFacts.add(fact);
    }

    public void clear() {
        hitFacts.clear();
        missFacts.clear();
    }

}
