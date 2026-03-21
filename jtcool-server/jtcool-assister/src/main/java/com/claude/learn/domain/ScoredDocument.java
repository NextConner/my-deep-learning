package com.claude.learn.domain;

import lombok.Data;

@Data
public class ScoredDocument {

    public String documentSegment;

    public double score;

    public String text;

    public float[] embedding;

    public float[] getEmbedding() {
        return embedding;
    }

    public ScoredDocument(String documentSegment, double score, String text) {
        this.documentSegment = documentSegment;
        this.score = score;
        this.text = text;
    }

    public ScoredDocument(String documentSegment, double score, float[] embedding) {
        this.documentSegment = documentSegment;
        this.score = score;
        this.embedding = embedding;
    }

}
