package com.github.davidfantasy.fastrule.executor;

import com.github.davidfantasy.fastrule.Rule;
import com.github.davidfantasy.fastrule.RuleManager;
import com.github.davidfantasy.fastrule.RulesEngineConfig;
import com.github.davidfantasy.fastrule.fact.Fact;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleTaskEventHandler implements WorkHandler<RuleTaskEvent> {

    @Override
    public void onEvent(RuleTaskEvent event) {
        Rule rule = event.getRule();
        if (rule != null) {
            executeWithSingleRule(rule, event.getFact());
        } else {
            executeWithPriorityRules(event.getRuleManager(), event.getFact(), event.getRulesEngineConfig());
        }
    }

    private void executeWithSingleRule(Rule rule, Fact fact) {
        if (!rule.preEvaluate(fact)) {
            log.debug("drop rule:{},{}", rule.getName(), fact.getId());
            return;
        }
        if (rule.evaluate(fact)) {
            log.debug("hit rule:{},{}", rule.getName(), fact.getId());
            rule.executeThen(fact);
        } else {
            log.debug("miss rule:{},{}", rule.getName(), fact.getId());
            rule.executeElse(fact);
        }
    }

    private void executeWithPriorityRules(RuleManager ruleManager, Fact fact, RulesEngineConfig config) {
        ruleManager.forEach(rule -> {
            if (rule.isEnabled()) {
                try {
                    if (!rule.preEvaluate(fact)) {
                        log.debug("drop rule:{},{}", rule.getName(), fact.getId());
                        return true;
                    }
                    boolean applied = rule.evaluate(fact);
                    if (applied) {
                        log.debug("hit rule:{},{}", rule.getName(), fact.getId());
                        rule.executeThen(fact);
                        if (config.isSkipOnFirstAppliedRule()) {
                            log.debug("next rules will be skipped since parameter skipOnFirstAppliedRule is set:{}", fact.getId());
                            return false;
                        }
                    } else {
                        log.debug("miss rule:{},{}", rule.getName(), fact.getId());
                        rule.executeElse(fact);
                        if (config.isSkipOnFirstNonAppliedRule()) {
                            log.debug("next rules will be skipped since parameter skipOnFirstNonAppliedRule is set:{}", fact.getId());
                            return false;
                        }
                    }
                } catch (Exception e) {
                    log.error("evaluate rule failed: " + rule.getName(), e);
                    if (config.isSkipOnFirstFailedRule()) {
                        log.debug("next rules will be skipped since parameter skipOnFirstFailedRule is set:{}", fact.getId());
                        return false;
                    }
                }
            }
            return true;
        });
    }

}



