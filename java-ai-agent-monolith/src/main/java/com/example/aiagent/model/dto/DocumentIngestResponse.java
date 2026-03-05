package com.example.aiagent.model.dto;

public record DocumentIngestResponse(
    long documentId,
    int chunksCreated
) {}

