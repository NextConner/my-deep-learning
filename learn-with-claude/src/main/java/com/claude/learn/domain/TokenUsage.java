package com.claude.learn.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_usage")
public class TokenUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private int inputTokens;
    private int outputTokens;
    private int totalTokens;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TokenUsage() {}

    public TokenUsage(String username, int inputTokens, int outputTokens) {
        this.username = username;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = inputTokens + outputTokens;
    }

    public String getUsername() { return username; }
    public int getInputTokens() { return inputTokens; }
    public int getOutputTokens() { return outputTokens; }
    public int getTotalTokens() { return totalTokens; }
}