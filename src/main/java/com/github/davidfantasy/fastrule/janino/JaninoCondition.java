package com.github.davidfantasy.fastrule.janino;

import com.github.davidfantasy.fastrule.condition.Condition;
import com.github.davidfantasy.fastrule.fact.Fact;
import lombok.Getter;
import org.codehaus.janino.ExpressionEvaluator;

import java.util.Arrays;

public class JaninoCondition implements Condition {

    @Getter
    private final String expression;

    private final ExpressionEvaluator ee;

    private final String[] paramNames;

    public JaninoCondition(String expression, String[] paramNames, Class<?>[] parameterTypes) {
        this.expression = expression;
        this.ee = new ExpressionEvaluator();
        this.paramNames = paramNames;
        //执行参数类型
        ee.setParameters(paramNames, parameterTypes);
        //设置返回类型
        ee.setExpressionType(Boolean.class);
        try {
            ee.cook(expression);
        } catch (Exception e) {
            throw new RuntimeException("expression compile failed：" + expression, e);
        }
    }

    @Override
    public boolean evaluate(Fact fact) {
        Object[] params = Arrays.stream(this.paramNames).map(fact::getValue).toArray();
        try {
            return (Boolean) ee.evaluate(params);
        } catch (Exception e) {
            throw new RuntimeException("evaluate rule error：" + this.getExpression(), e);
        }
    }


}
