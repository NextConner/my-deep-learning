package com.claude.learn.agent.invoker;

import com.claude.learn.config.ModelsProperties;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(ModelsProperties.class)
public class ModelClientFactory {

    private final ModelsProperties properties;

    public ModelClientFactory(ModelsProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Qualifier("primaryClient")
    public LanguageModelClient primaryClient() {
        return buildClient(properties.getPrimary());
    }

    @Bean
    @Qualifier("fallbackClient")
    public LanguageModelClient fallbackClient() {
        return buildClient(properties.getFallback());
    }

    private LanguageModelClient buildClient(ModelsProperties.ModelConfig config) {
        return switch (config.getProvider()) {
            case "openai-compatible" -> buildOpenAiClient(config);
            case "anthropic"         -> buildAnthropicClient(config);
            default -> throw new IllegalArgumentException(
                    "Unknown provider: " + config.getProvider());
        };
    }

    // OpenAI 兼容格式：Deepseek、Ollama、通义千问等
    private LanguageModelClient buildOpenAiClient(ModelsProperties.ModelConfig config) {
        ChatLanguageModel model = OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .maxTokens(config.getMaxTokens())
                .build();
        return new LangChain4jModelClient(model, config.getModelName());
    }

    // Anthropic 格式：Claude 系列
    private LanguageModelClient buildAnthropicClient(ModelsProperties.ModelConfig config) {
        ChatLanguageModel model = AnthropicChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .maxTokens(config.getMaxTokens())
                .build();
        return new LangChain4jModelClient(model, config.getModelName());
    }
}