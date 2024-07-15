package com.github.davidfantasy.fastrule.condition;

import com.github.davidfantasy.fastrule.fact.Fact;

import java.util.List;

public class AndCondition extends CompositeCondition {

    public AndCondition(List<Condition> conditions) {
        super(conditions);
    }

    @Override
    public boolean evaluate(Fact fact) {
        for (Condition condition : conditions) {
            if (!condition.evaluate(fact)) {
                return false;
            }
        }
        return true;
    }

}