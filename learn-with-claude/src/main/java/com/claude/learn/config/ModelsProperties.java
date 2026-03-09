package com.claude.learn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.models")
@Data
public class ModelsProperties {
    private ModelConfig primary;
    private ModelConfig fallback;

    @Data
    public static class ModelConfig {
        private String provider;  // openai-compatible | anthropic
        private String baseUrl;
        private String apiKey;
        private String modelName;
        private int maxTokens;
    }
}