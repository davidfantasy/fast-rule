package com.github.davidfantasy.fastrule;

import com.github.davidfantasy.fastrule.fact.SimpleFact;
import com.github.davidfantasy.fastrule.mock.SimpleDelayStatefulTriggerRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestDelayStatefulTriggerRule {

    private RuleEngine ruleEngine;

    private RuleManager ruleManager;

    @BeforeEach
    public void beforeEachTest() {
        ruleManager = new DefaultRuleManager();
        ruleEngine = new DefaultRuleEngine(ruleManager, RulesEngineConfig.builder().build());
        ruleEngine.start();
    }

    @Test
    public void testTriggerOnce() throws InterruptedException {
        SimpleDelayStatefulTriggerRule rule = new SimpleDelayStatefulTriggerRule("rule1", (fact) -> {
            int v = Integer.parseInt(fact.getValue("v").toString());
            return v < 10;
        }, null);
        rule.enable();
        ruleManager.add(rule);
        ruleEngine.fire(new SimpleFact("fact1", "v", 7, null), true);
        Thread.sleep(100);
        Assertions.assertEquals(1, rule.getHitFacts().size());
        //重复触发，忽略
        ruleEngine.fire(new SimpleFact("fact1", "v", 8, null), true);
        Thread.sleep(100);
        Assertions.assertEquals(1, rule.getHitFacts().size());
        //恢复该规则对应的触发状态
        ruleEngine.fire(new SimpleFact("fact1", "v", 11, null), true);
        Thread.sleep(100);
        Assertions.assertEquals(1, rule.getHitFacts().size());
        //会重新触发
        ruleEngine.fire(new SimpleFact("fact1", "v", 8, null), true);
        Thread.sleep(100);
        Assertions.assertEquals(2, rule.getHitFacts().size());
    }

    @Test
    public void testTriggerDelay() throws InterruptedException {
        SimpleDelayStatefulTriggerRule rule = new SimpleDelayStatefulTriggerRule("rule1", (fact) -> {
            int v = Integer.parseInt(fact.getValue("v").toString());
            return v < 10;
        }, 1000L);
        rule.enable();
        ruleManager.add(rule);
        ruleEngine.fire(new SimpleFact("fact1", "v", 7, null), true);
        Thread.sleep(100);
        //延迟中，没有触发记录
        Assertions.assertEquals(0, rule.getHitFacts().size());
        Thread.sleep(1000);
        //延迟期满，产生触发记录
        Assertions.assertEquals(1, rule.getHitFacts().size());
    }

}
