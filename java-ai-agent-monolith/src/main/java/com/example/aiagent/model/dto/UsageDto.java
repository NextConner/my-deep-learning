package com.example.aiagent.model.dto;

public record UsageDto(
    String provider,
    String model,
    Integer promptTokens,
    Integer completionTokens,
    Integer latencyMs
) {}

