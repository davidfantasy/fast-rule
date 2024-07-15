package com.github.davidfantasy.fastrule;

import cn.hutool.core.lang.Assert;
import com.github.davidfantasy.fastrule.condition.Condition;
import com.github.davidfantasy.fastrule.fact.Fact;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseRule implements Rule {

    private final Condition condition;

    private final String id;

    private final String name;

    private final int priority;

    private final String description;

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    private Set<String> concernedFacts;

    public BaseRule(String id, String name, Integer priority, String description, Condition condition) {
        this.condition = condition;
        this.id = id;
        Assert.notNull(condition, "condition must not be null");
        Assert.notNull(id, "id must not be null");
        this.name = name;
        if (priority != null) {
            this.priority = priority;
        } else {
            this.priority = DEFAULT_PRIORITY;
        }
        this.description = description;
        this.enable();
    }

    /**
     * 添加规则关注的factId
     */
    public void addConcernedFact(String factId) {
        if (this.concernedFacts == null) {
            this.concernedFacts = new HashSet<>();
        }
        this.concernedFacts.add(factId);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean preEvaluate(Fact fact) {
        return concernedFacts == null || concernedFacts.contains(fact.getId());
    }

    @Override
    public boolean evaluate(Fact fact) {
        return condition.evaluate(fact);
    }

    @Override
    public void executeElse(Fact fact) {
        //do nothing
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public void enable() {
        enabled.set(true);
    }

    @Override
    public void disable() {
        enabled.set(false);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseRule)) return false;
        BaseRule baseRule = (BaseRule) o;
        return Objects.equals(getId(), baseRule.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
