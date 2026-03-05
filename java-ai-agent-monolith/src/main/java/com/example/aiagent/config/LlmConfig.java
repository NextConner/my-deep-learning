package com.example.aiagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 配置类
 * 从 application.yml 读取 LLM 相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.llm")
public class LlmConfig {

    /** 当前使用的 LLM 提供商 (deepseek/openai) */
    private String provider = "deepseek";
    
    /** 请求超时时间(毫秒) */
    private long timeoutMs = 60000;

    /** OpenAI 配置 */
    private OpenAiConfig openai = new OpenAiConfig();
    
    /** DeepSeek 配置 */
    private DeepSeekConfig deepseek = new DeepSeekConfig();
    
    /** 阿里百炼配置(Embedding) */
    private BailianConfig bailian = new BailianConfig();

    /**
     * OpenAI 配置类
     */
    @Data
    public static class OpenAiConfig {
        /** OpenAI API Key */
        private String apiKey;
        /** 使用的模型 */
        private String model = "gpt-4.1-mini";
        /** API 基础地址 */
        private String baseUrl = "https://api.openai.com";
    }

    /**
     * DeepSeek 配置类
     */
    @Data
    public static class DeepSeekConfig {
        /** DeepSeek API Key */
        private String apiKey;
        /** 使用的模型 */
        private String model = "deepseek-chat";
        /** API 基础地址 */
        private String baseUrl = "https://api.deepseek.com";
    }

    /**
     * 阿里百炼配置类
     */
    @Data
    public static class BailianConfig {
        /** 百炼 API Key */
        private String apiKey;
        /** Embedding 模型 */
        private String embeddingModel = "text-embedding-v3";
        /** API 基础地址 */
        private String baseUrl = "https://dashscope.aliyuncs.com";
    }
}
