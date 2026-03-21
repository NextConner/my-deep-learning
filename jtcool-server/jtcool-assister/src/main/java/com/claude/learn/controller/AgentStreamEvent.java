package com.claude.learn.controller;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AgentStreamEvent.StepStartEvent.class, name = "step_start"),
        @JsonSubTypes.Type(value = AgentStreamEvent.StepCompleteEvent.class, name = "step_complete"),
        @JsonSubTypes.Type(value = AgentStreamEvent.TokenEvent.class, name = "token"),
        @JsonSubTypes.Type(value = AgentStreamEvent.CompleteEvent.class, name = "complete"),
        @JsonSubTypes.Type(value = AgentStreamEvent.ErrorEvent.class, name = "error")
})
public sealed interface AgentStreamEvent {

    record StepStartEvent(
            String stepId,
            int sequence,
            String toolName,
            String inputSummary
    ) implements AgentStreamEvent {}

    record StepCompleteEvent(
            String stepId,
            int sequence,
            String status,
            long latencyMs,
            String outputSummary
    ) implements AgentStreamEvent {}

    record TokenEvent(
            String token
    ) implements AgentStreamEvent {}

    record CompleteEvent(
            String runId,
            String answer,
            int totalSteps,
            long totalLatencyMs
    ) implements AgentStreamEvent {}

    record ErrorEvent(
            String runId,
            String error,
            String errorCode
    ) implements AgentStreamEvent {}
}
