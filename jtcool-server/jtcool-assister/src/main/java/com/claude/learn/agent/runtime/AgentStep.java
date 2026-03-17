package com.claude.learn.agent.runtime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class AgentStep {

    private final String stepId;
    private final int sequence;
    private final String toolName;
    private final String inputSummary;
    private AgentStepStatus status;
    private String outputSummary;
    private AgentErrorCode errorCode;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public AgentStep(int sequence, String toolName, String inputSummary) {
        this.stepId = UUID.randomUUID().toString();
        this.sequence = sequence;
        this.toolName = toolName;
        this.inputSummary = inputSummary;
        this.status = AgentStepStatus.PLANNED;
        this.errorCode = AgentErrorCode.NONE;
    }

    public void start() {
        this.status = AgentStepStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void markSuccess(String outputSummary) {
        this.status = AgentStepStatus.SUCCESS;
        this.outputSummary = outputSummary;
        this.endedAt = LocalDateTime.now();
    }

    public void markFailed(AgentErrorCode errorCode, String errorMessage) {
        this.status = AgentStepStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.endedAt = LocalDateTime.now();
    }

    public void markTimeout(String errorMessage) {
        this.status = AgentStepStatus.TIMEOUT;
        this.errorCode = AgentErrorCode.TOOL_TIMEOUT;
        this.errorMessage = errorMessage;
        this.endedAt = LocalDateTime.now();
    }

    public long latencyMs() {
        if (startedAt == null || endedAt == null) {
            return 0L;
        }
        return Duration.between(startedAt, endedAt).toMillis();
    }

    public String getStepId() {
        return stepId;
    }

    public int getSequence() {
        return sequence;
    }

    public String getToolName() {
        return toolName;
    }

    public String getInputSummary() {
        return inputSummary;
    }

    public AgentStepStatus getStatus() {
        return status;
    }

    public String getOutputSummary() {
        return outputSummary;
    }

    public AgentErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }
}
