package com.claude.learn.controller;

import com.claude.learn.agent.runtime.AgentRun;
import com.claude.learn.agent.runtime.AgentStep;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record ChatResponse(
        String answer,
        String runId,
        String status,
        long totalLatencyMs,
        int totalSteps,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<StepInfo> steps
) {

    public static ChatResponse from(AgentRun run, boolean includeSteps) {
        return new ChatResponse(
                run.getFinalAnswer(),
                run.getRunId(),
                run.getStatus().name(),
                run.totalLatencyMs(),
                run.getSteps().size(),
                run.getStartedAt(),
                run.getEndedAt(),
                includeSteps ? run.getSteps().stream()
                        .map(StepInfo::from)
                        .collect(Collectors.toList()) : null
        );
    }

    public record StepInfo(
            String stepId,
            int sequence,
            String toolName,
            String status,
            long latencyMs,
            String inputSummary,
            String outputSummary,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        public static StepInfo from(AgentStep step) {
            return new StepInfo(
                    step.getStepId(),
                    step.getSequence(),
                    step.getToolName(),
                    step.getStatus().name(),
                    step.latencyMs(),
                    step.getInputSummary(),
                    step.getOutputSummary(),
                    step.getErrorMessage(),
                    step.getStartedAt(),
                    step.getEndedAt()
            );
        }
    }
}

