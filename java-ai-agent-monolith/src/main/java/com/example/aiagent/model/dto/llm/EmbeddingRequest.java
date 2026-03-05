package com.example.aiagent.model.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Bailian (百炼) embedding request
 */
public record EmbeddingRequest(
    String model,
    List<String> input
) {}
