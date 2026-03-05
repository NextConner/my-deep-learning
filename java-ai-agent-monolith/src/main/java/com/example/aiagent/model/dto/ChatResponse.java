package com.example.aiagent.model.dto;

import java.util.List;

public record ChatResponse(
    String answer,
    List<CitationDto> citations,
    UsageDto usage
) {}

