package com.claude.learn.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定义agent 指标
 */
@Component
public class AgentMetrics {

    private final Counter successCounter;
    private final Counter failureCounter;
    private final Timer latencyTimer;
    private final Counter tokenCounter;
    private final Counter cacheHitCounter;

    public AgentMetrics(MeterRegistry registry) {
        this.successCounter = registry.counter("agent.run.success");
        this.failureCounter = registry.counter("agent.run.failure");
        this.latencyTimer   = registry.timer("agent.run.latency");
        this.tokenCounter   = registry.counter("agent.token.consumed");
        this.cacheHitCounter = registry.counter("agent.cache.hit");
    }

    public void recordSuccess(long latencyMs) {
        successCounter.increment();
        latencyTimer.record(latencyMs, TimeUnit.MILLISECONDS);
    }

    public void recordFailure() { failureCounter.increment(); }
    public void recordTokens(double count) { tokenCounter.increment(count); }
    public void recordCacheHit() { cacheHitCounter.increment(); }
}
