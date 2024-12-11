package com.github.davidfantasy.fastrule.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RateLimitedLogger {

    private final long timeWindowMillis;
    private final int maxLogsPerWindow;
    private final AtomicLong lastLogTime = new AtomicLong(0);
    private final AtomicInteger logCount = new AtomicInteger(0);

    public RateLimitedLogger(long timeWindowMillis, int maxLogsPerWindow) {
        this.timeWindowMillis = timeWindowMillis;
        this.maxLogsPerWindow = maxLogsPerWindow;
    }

    public void logError(String format, Object... arguments) {
        long now = System.currentTimeMillis();
        long lastTime = lastLogTime.get();

        if (now - lastTime > timeWindowMillis) {
            logCount.set(0);
            lastLogTime.set(now);
        }
        if (logCount.incrementAndGet() <= maxLogsPerWindow) {
            log.error(format, arguments);
        }
    }

    public void logError(String msg, Throwable throwable) {
        long now = System.currentTimeMillis();
        long lastTime = lastLogTime.get();
        if (now - lastTime > timeWindowMillis) {
            logCount.set(0);
            lastLogTime.set(now);
        }
        if (logCount.incrementAndGet() <= maxLogsPerWindow) {
            log.error(msg, throwable);
        }
    }

}

