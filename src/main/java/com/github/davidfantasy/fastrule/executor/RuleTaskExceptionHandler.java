package com.github.davidfantasy.fastrule.executor;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleTaskExceptionHandler implements ExceptionHandler<RuleTaskEvent> {
    @Override
    public void handleEventException(Throwable ex, long sequence, RuleTaskEvent event) {
        log.warn("handle rule event error", ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("RuleTaskExceptionHandler start error", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("RuleTaskExceptionHandler shutdown error", ex);
    }

}
