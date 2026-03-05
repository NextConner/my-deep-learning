package com.example.aiagent.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank String tenantId,
    @NotBlank String userId,
    @NotBlank String message
) {}

