package com.github.davidfantasy.fastrule.fact;


import java.util.List;

public interface FactCollector {
    /**
     * 采集器的名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取当前产生的所有facts
     */
    List<Fact> collect();

    /**
     * 启动当前的收集器，收集器在启动后才会产生Fact数据
     */
    void startup();

    /**
     * 关闭当前的收集器，收集器停止后不再产生Fact数据
     */
    void shutdown();
}
