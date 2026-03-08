package com.claude.learn.service;

import com.claude.learn.repository.PromptConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PromptService {

    private static final Logger log = LoggerFactory.getLogger(PromptService.class);

    private final PromptConfigRepository repository;

    public PromptService(PromptConfigRepository repository) {
        this.repository = repository;
    }

    public String getPrompt(String agentName, String defaultPrompt) {
        return repository.findByAgentName(agentName)
                .map(config -> {
                    log.info("📝 加载动态提示词：{}", agentName);
                    return config.getSystemPrompt();
                })
                .orElseGet(() -> {
                    log.info("📝 使用默认提示词：{}", agentName);
                    return defaultPrompt;
                });
    }
}