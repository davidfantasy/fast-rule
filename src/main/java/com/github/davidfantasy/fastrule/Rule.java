package com.github.davidfantasy.fastrule;

import com.github.davidfantasy.fastrule.fact.Fact;

/**
 * 业务规则
 */
public interface Rule extends Comparable<Rule> {

    /**
     * 默认名称
     */
    String DEFAULT_NAME = "rule";

    /**
     * 默认的规则优先级
     */
    int DEFAULT_PRIORITY = Integer.MAX_VALUE - 1;


    /**
     * 规则的唯一ID
     */
    String getId();

    /**
     * 规则名称
     */
    default String getName() {
        return DEFAULT_NAME;
    }

    /**
     * 规则描述
     */
    default String getDescription() {
        return "";
    }

    /**
     * 该规则的优先级
     */
    default int getPriority() {
        return DEFAULT_PRIORITY;
    }

    /**
     * 该规则当前是否生效
     */
    boolean isEnabled();

    /**
     * 启用规则
     */
    void enable();

    /**
     * 禁用规则
     */
    void disable();

    /**
     * 在evaluate之前执行，可以根据fact类型或id初步判断是否匹配当前的规则，
     * 当规则依赖于其它业务数据时，也可以在此处对fact的值进行完善和补充，让evaluate能够顺利的执行
     */
    boolean preEvaluate(Fact fact);

    /**
     * 执行条件判断，以验证某个fact是否满足某个规则的触发条件
     */
    boolean evaluate(Fact fact);

    /**
     * 当满足规则条件时触发，即evaluate方法返回true
     */
    void executeThen(Fact fact);

    /**
     * 当不满足规则条件时触发，即evaluate方法返回false，用于处理某些状态恢复的情况，比如告警恢复
     */
    void executeElse(Fact fact);

    /**
     * 比较规则优先级，默认按降序排列
     */
    @Override
    default int compareTo(Rule other) {
        return Integer.compare(other.getPriority(),this.getPriority());
    }

}
