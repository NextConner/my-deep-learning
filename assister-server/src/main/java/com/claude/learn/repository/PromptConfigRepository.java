package com.claude.learn.repository;

import com.claude.learn.domain.PromptConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromptConfigRepository extends JpaRepository<PromptConfig, Long> {
    Optional<PromptConfig> findByAgentName(String agentName);
}