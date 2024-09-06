package com.github.davidfantasy.fastrule;

import com.github.davidfantasy.fastrule.condition.Condition;
import com.github.davidfantasy.fastrule.fact.SimpleFact;
import com.github.davidfantasy.fastrule.janino.JaninoCondition;
import com.github.davidfantasy.fastrule.janino.JaninoRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TestJaninoRule {

    private RuleEngine ruleEngine;

    private RuleManager ruleManager;

    @BeforeEach
    public void beforeEachTest() {
        ruleManager = new DefaultRuleManager();
        ruleEngine = new DefaultRuleEngine(ruleManager, RulesEngineConfig.builder().build());
        ruleEngine.start();
    }

    @Test
    public void testRule() throws InterruptedException {
        SimpleFact fact = new SimpleFact("fact1", "param", 20, null);
        Map<String, Integer> resultMap = new HashMap<>();
        fact.addValue("result", resultMap);
        String executeScript = "result.put(\"number\", param*20);";
        String conditionExpr = "param > 10";
        Condition condition = new JaninoCondition(conditionExpr, new String[]{"param"}, new Class[]{Integer.class});
        JaninoRule rule = new JaninoRule("rule1", "rule1", 1, "rule1", condition, executeScript, new String[]{"param", "result"}, new Class[]{Integer.class, Map.class});
        rule.enable();
        ruleManager.add(rule);
        ruleEngine.fire(fact, false);
        Thread.sleep(100);
        Assertions.assertEquals(400, resultMap.get("number"));
    }


}
