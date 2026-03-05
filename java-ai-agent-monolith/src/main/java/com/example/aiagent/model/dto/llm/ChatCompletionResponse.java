package com.example.aiagent.model.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * OpenAI-compatible chat completion response
 */
public record ChatCompletionResponse(
    String id,
    String object,
    long created,
    String model,
    List<Choice> choices,
    Usage usage,
    @JsonProperty("finish_reason")
    String finishReason
) {
    public record Choice(
        int index,
        Message message,
        @JsonProperty("finish_reason")
        String finishReason
    ) {}

    public record Message(
        String role,
        String content,
        @JsonProperty("tool_calls")
        List<ToolCall> toolCalls
    ) {}

    public record ToolCall(
        String id,
        String type,
        FunctionCall function
    ) {}

    public record FunctionCall(
        String name,
        String arguments
    ) {}

    public record Usage(
        @JsonProperty("prompt_tokens")
        int promptTokens,
        @JsonProperty("completion_tokens")
        int completionTokens,
        @JsonProperty("total_tokens")
        int totalTokens
    ) {}
}
