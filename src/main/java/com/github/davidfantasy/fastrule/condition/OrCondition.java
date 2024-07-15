package com.github.davidfantasy.fastrule.condition;

import com.github.davidfantasy.fastrule.fact.Fact;

import java.util.List;

public class OrCondition extends CompositeCondition {

    public OrCondition(List<Condition> conditions) {
        super(conditions);
    }

    @Override
    public boolean evaluate(Fact fact) {
        for (Condition condition : conditions) {
            if (condition.evaluate(fact)) {
                return true;
            }
        }
        return false;
    }

}