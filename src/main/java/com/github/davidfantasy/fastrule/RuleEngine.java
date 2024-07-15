package com.github.davidfantasy.fastrule;

import com.github.davidfantasy.fastrule.fact.Fact;
import com.github.davidfantasy.fastrule.fact.FactCollector;

public interface RuleEngine {

    /**
     * 负责对具体的规则进行管理，规则引擎依靠其获取规则数据
     */
    RuleManager getRuleManager();

    /**
     * 规则引擎的一些全局配置
     */
    RulesEngineConfig getConfig();

    void start();

    void shutdown();

    /**
     * 直接传入事实数据进行规则匹配
     *
     * @param fact            事实数据
     * @param firedByPriority 是否要确保按优先级顺序执行规则，确保匹配顺序会消耗额外的性能，请根据需要选择是否开启
     */
    void fire(Fact fact, boolean firedByPriority);

    /**
     * 注册fact采集器进行周期的事实数据采集，采集后的数据再和规则进行匹配执行，通过批量处理提升执行效率
     *
     * @param collectIntervalMs 事实采集器的周期采集时间，单位毫秒
     * @param collector         采集器
     * @param firedByPriority   是否要确保按优先级顺序执行规则，和fire()中的含义一致
     */
    void fireByCollector(long collectIntervalMs, FactCollector collector, boolean firedByPriority);


    /**
     * 停止并移除某个已添加的FactCollector
     */
    void removeFactCollector(String name);

}
