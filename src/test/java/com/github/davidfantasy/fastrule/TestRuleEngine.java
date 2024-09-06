package com.github.davidfantasy.fastrule;


import com.github.davidfantasy.fastrule.fact.Fact;
import com.github.davidfantasy.fastrule.fact.SimpleFact;
import com.github.davidfantasy.fastrule.mock.MockManualCollector;
import com.github.davidfantasy.fastrule.mock.SimpleTestRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestRuleEngine {

    private RuleEngine ruleEngine;

    private RuleManager ruleManager;

    @BeforeEach
    public void beforeEachTest() {
        ruleManager = new DefaultRuleManager();
        ruleEngine = new DefaultRuleEngine(ruleManager, RulesEngineConfig.builder().build());
        ruleEngine.start();
    }

    @Test
    public void testFire() throws InterruptedException {
        var rule = new SimpleTestRule("rule1", 1, fact -> {
            int v = Integer.parseInt(fact.getValue("v").toString());
            return v > 5 && v < 10;
        });
        rule.enable();
        ruleManager.add(rule);
        List<Fact> facts = new ArrayList<>();
        facts.add(new SimpleFact("fact1", "v", 7, null));
        facts.add(new SimpleFact("fact2", "v", 12, null));
        facts.add(new SimpleFact("fact3", "v", 9, null));
        facts.add(new SimpleFact("fact4", "v", 4, null));
        facts.forEach(fact -> ruleEngine.fire(fact, false));
        Thread.sleep(500);
        rule = (SimpleTestRule) ruleManager.get("rule1");
        var hittedFacts =  rule.getHitFacts().stream().map(Fact::getId).toList();
        var missedFacts =  rule.getMissFacts().stream().map(Fact::getId).toList();
        Assertions.assertTrue(hittedFacts.contains("fact1"));
        Assertions.assertTrue(hittedFacts.contains("fact3"));
        Assertions.assertTrue(missedFacts.contains("fact2"));
        Assertions.assertTrue(missedFacts.contains("fact4"));
    }

    @Test
    public void testFireByCollector() throws InterruptedException {
        MockManualCollector collector = new MockManualCollector();
        SimpleTestRule rule1 = new SimpleTestRule("rule1", 1, fact -> {
            int v = Integer.parseInt(fact.getValue("v").toString());
            return v > 5 && v < 10;
        });
        ruleManager.add(rule1);
        ruleEngine.fireByCollector(100, collector, false);
        collector.mockFact("v", 7);
        collector.mockFact("v", 4);
        Thread.sleep(500);
        Assertions.assertEquals(1, rule1.getHitFacts().size());
    }

    @Test
    public void testRemoveCollector() throws InterruptedException {
        MockManualCollector collector = new MockManualCollector();
        SimpleTestRule rule1 = new SimpleTestRule("rule1", 1, fact -> {
            int v = Integer.parseInt(fact.getValue("v").toString());
            return v > 5 && v < 10;
        });
        ruleManager.add(rule1);
        ruleEngine.fireByCollector(100, collector, false);
        collector.mockFact("v", 7);
        Thread.sleep(500);
        Assertions.assertEquals(1, rule1.getHitFacts().size());
        ruleEngine.removeFactCollector(MockManualCollector.class.getSimpleName());
        Thread.sleep(100);
        collector.mockFact("v", 8);
        Thread.sleep(500);
        Assertions.assertEquals(1, rule1.getHitFacts().size());
    }

    @Test
    public void testSkipOnFirstAppliedRule() throws InterruptedException {
        RulesEngineConfig config = RulesEngineConfig.builder().skipOnFirstAppliedRule(true).build();
        ruleEngine = new DefaultRuleEngine(ruleManager, config);
        ruleEngine.start();
        SimpleTestRule rule1 = new SimpleTestRule("rule1", 1, fact -> true);
        SimpleTestRule rule2 = new SimpleTestRule("rule2", 2, fact -> true);
        SimpleTestRule rule3 = new SimpleTestRule("rule3", 3, fact -> true);
        ruleManager.add(rule2);
        ruleManager.add(rule3);
        ruleManager.add(rule1);
        ruleEngine.fire(new SimpleFact("fact1", "v", 7, null), true);
        Thread.sleep(500);
        Assertions.assertEquals(1, rule3.getHitFacts().size());
        Assertions.assertEquals(0, rule2.getHitFacts().size());
        Assertions.assertEquals(0, rule1.getHitFacts().size());
    }

    @Test
    public void testskipOnFirstNonAppliedRule() throws InterruptedException {
        RulesEngineConfig config = RulesEngineConfig.builder().skipOnFirstNonAppliedRule(true).build();
        ruleEngine = new DefaultRuleEngine(ruleManager, config);
        ruleEngine.start();
        SimpleTestRule rule1 = new SimpleTestRule("rule1", 1, fact -> true);
        SimpleTestRule rule2 = new SimpleTestRule("rule2", 2, fact -> true);
        SimpleTestRule rule3 = new SimpleTestRule("rule3", 3, fact -> false);
        ruleManager.add(rule2);
        ruleManager.add(rule3);
        ruleManager.add(rule1);
        ruleEngine.fire(new SimpleFact("fact1", "v", 7, null), true);
        Thread.sleep(500);
        Assertions.assertEquals(0, rule3.getHitFacts().size());
        Assertions.assertEquals(0, rule2.getHitFacts().size());
        Assertions.assertEquals(0, rule1.getHitFacts().size());
    }

    @Test
    public void testskipOnFirstFailedRule() throws InterruptedException {
        RulesEngineConfig config = RulesEngineConfig.builder().skipOnFirstFailedRule(true).build();
        ruleEngine = new DefaultRuleEngine(ruleManager, config);
        ruleEngine.start();
        SimpleTestRule rule1 = new SimpleTestRule("rule1", 1, fact -> true);
        SimpleTestRule rule2 = new SimpleTestRule("rule2", 2, fact -> true);
        SimpleTestRule rule3 = new SimpleTestRule("rule3", 3, fact -> {
            throw new RuntimeException("expected exception");
        });
        ruleManager.add(rule2);
        ruleManager.add(rule3);
        ruleManager.add(rule1);
        ruleEngine.fire(new SimpleFact("fact1", "v", 7, null), true);
        Thread.sleep(500);
        Assertions.assertEquals(0, rule3.getHitFacts().size());
        Assertions.assertEquals(0, rule2.getHitFacts().size());
        Assertions.assertEquals(0, rule1.getHitFacts().size());
    }

}
