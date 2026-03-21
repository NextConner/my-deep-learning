package com.claude.learn.agent.tool;

public class ToolExecutionResult {

    private final boolean success;
    private final String output;
    private final String errorMessage;
    private final long executionTimeMs;

    public static ToolExecutionResult success(String output, long executionTimeMs) {
        return new ToolExecutionResult(true, output, null, executionTimeMs);
    }

    public static ToolExecutionResult failure(String errorMessage, long executionTimeMs) {
        return new ToolExecutionResult(false, null, errorMessage, executionTimeMs);
    }

    private ToolExecutionResult(boolean success, String output, String errorMessage, long executionTimeMs) {
        this.success = success;
        this.output = output;
        this.errorMessage = errorMessage;
        this.executionTimeMs = executionTimeMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
}
