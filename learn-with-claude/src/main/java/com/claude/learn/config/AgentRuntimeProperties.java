package com.claude.learn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "agent.runtime")
public class AgentRuntimeProperties {

    private int maxSteps = 3;
    private long stepTimeoutMs = 15000L;
    private int retryTimes = 1;
    private boolean includeTraceInResponse = true;

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public long getStepTimeoutMs() {
        return stepTimeoutMs;
    }

    public void setStepTimeoutMs(long stepTimeoutMs) {
        this.stepTimeoutMs = stepTimeoutMs;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public boolean isIncludeTraceInResponse() {
        return includeTraceInResponse;
    }

    public void setIncludeTraceInResponse(boolean includeTraceInResponse) {
        this.includeTraceInResponse = includeTraceInResponse;
    }
}
