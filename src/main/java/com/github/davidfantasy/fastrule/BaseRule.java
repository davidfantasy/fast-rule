package com.github.davidfantasy.fastrule;

import cn.hutool.core.lang.Assert;
import com.github.davidfantasy.fastrule.condition.Condition;
import com.github.davidfantasy.fastrule.fact.Fact;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class BaseRule implements Rule {

    protected String id;

    protected String name;

    protected int priority;

    protected Condition condition;

    protected String description;

    private Set<String> concernedFacts;

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    public BaseRule(String id, String name, Integer priority, String description, Condition condition) {
        Assert.notNull(condition, "condition must not be null");
        Assert.notNull(id, "id must not be null");
        this.id = id;
        this.name = name;
        this.condition = condition;
        this.priority = Objects.requireNonNullElse(priority, DEFAULT_PRIORITY);
        this.description = description;
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

    public void clearConcernedFacts() {
        this.concernedFacts = null;
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
        if (!this.isEnabled()) {
            log.warn("try to evaluate a disabled rule：{}，{}", this.name, this.id);
            return false;
        }
        try {
            return this.condition.evaluate(fact);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("rule condition evaluation encountered an error：{}，{}，{}", this.name, fact.getId(), e.getMessage());
        }
        return false;
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
        if (!(o instanceof BaseRule baseRule)) return false;
        return Objects.equals(getId(), baseRule.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
