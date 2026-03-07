package com.claude.learn.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_config")
public class PromptConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String agentName;

    @Column(columnDefinition = "TEXT")
    private String systemPrompt;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public PromptConfig() {}

    public String getAgentName() { return agentName; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        this.updatedAt = LocalDateTime.now();
    }
}