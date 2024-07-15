package com.github.davidfantasy.fastrule.condition;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeCondition implements Condition {

    protected List<Condition> conditions;

    public CompositeCondition(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public void addCondition(Condition condition) {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }
        this.conditions.add(condition);
    }

}
