package com.github.davidfantasy.fastrule.executor;

import com.github.davidfantasy.fastrule.Rule;
import com.github.davidfantasy.fastrule.RuleManager;
import com.github.davidfantasy.fastrule.RulesEngineConfig;
import com.github.davidfantasy.fastrule.fact.Fact;
import lombok.Data;

@Data
public class RuleTaskEvent {

    private Fact fact;

    private Rule rule;

    private RulesEngineConfig rulesEngineConfig;

    private RuleManager ruleManager;

}
