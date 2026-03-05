package com.example.aiagent.model.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentIngestRequest(
    @NotBlank String tenantId,
    @NotBlank String title,
    String source,
    @NotBlank String content
) {}

