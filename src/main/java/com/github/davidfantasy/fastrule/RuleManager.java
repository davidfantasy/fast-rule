package com.github.davidfantasy.fastrule;

public interface RuleManager {

    void add(Rule rule);

    Rule remove(String ruleId);

    Rule get(String ruleId);

    void forEach(RuleConsumer consumer);

    void clear();

}
