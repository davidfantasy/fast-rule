
package com.github.davidfantasy.fastrule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RulesEngineConfig {
    /**
     * 当fact已触发第一个规则后，是否跳过对后续的规则的匹配，仅当启用规则优先级匹配时生效
     */
    private boolean skipOnFirstAppliedRule;
    /**
     * 当fact没有触发第一个规则时，是否跳过对后续的规则的匹配，仅当启用规则优先级匹配时生效
     */
    private boolean skipOnFirstNonAppliedRule;
    /**
     * 当fact触发第一个规则执行发生异常时，是否跳过后续的规则，仅当启用规则优先级匹配时生效
     */
    private boolean skipOnFirstFailedRule;
    /**
     * 用于周期调度factCollector的线程池大小
     */
    private int collectorScheduledThreadPoolSize = Runtime.getRuntime().availableProcessors() * 2;

}
