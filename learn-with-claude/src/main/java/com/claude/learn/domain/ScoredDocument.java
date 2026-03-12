package com.claude.learn.domain;

/**
 * 权重文档结构
 * @param text
 * @param score
 */
public record ScoredDocument(
        String text,
        double score,
        float[] embedding
) {}
