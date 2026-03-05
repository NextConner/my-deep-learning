package com.example.aiagent.model.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible chat completion request
 */
public record ChatCompletionRequest(
    String model,
    List<Message> messages,
    Double temperature,
    @JsonProperty("max_tokens")
    Integer maxTokens,
    Boolean stream,
    List<Tool> tools
) {
    public record Message(
        String role,
        String content,
        List<ToolCall> tool_calls,
        @JsonProperty("tool_call_id")
        String tool_call_id
    ) {}

    public record Tool(
        String type,
        Function function
    ) {}

    public record Function(
        String name,
        String description,
        Map<String, Object> parameters
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
}
