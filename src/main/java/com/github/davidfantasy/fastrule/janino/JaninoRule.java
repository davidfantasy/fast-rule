package com.github.davidfantasy.fastrule.janino;

import com.github.davidfantasy.fastrule.BaseRule;
import com.github.davidfantasy.fastrule.condition.Condition;
import com.github.davidfantasy.fastrule.fact.Fact;
import org.codehaus.janino.ScriptEvaluator;

import java.util.Arrays;

public class JaninoRule extends BaseRule {

    private final ScriptEvaluator se;

    private final String[] paramNames;

    public JaninoRule(String id,
                      String name,
                      Integer priority,
                      String description,
                      Condition condition,
                      String executeScript,
                      String[] paramNames,
                      Class<?>[] paramTypes) {
        super(id, name, priority, description, condition);
        se = new ScriptEvaluator();
        this.paramNames = paramNames;
        se.setParameters(paramNames, paramTypes);
        try {
            se.cook(executeScript);
        } catch (Exception e) {
            throw new RuntimeException("script compile failed：" + executeScript, e);
        }
    }

    @Override
    public void executeThen(Fact fact) {
        Object[] params = Arrays.stream(this.paramNames).map(fact::getValue).toArray();
        try {
            se.evaluate(params);
        } catch (Exception e) {
            throw new RuntimeException("rule " + this.getName() + " execute failed", e);
        }
    }


}
