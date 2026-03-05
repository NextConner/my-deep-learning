package com.example.aiagent.model.dto;

public record CitationDto(
    long documentId,
    long chunkId,
    String title,
    String snippet
) {}

