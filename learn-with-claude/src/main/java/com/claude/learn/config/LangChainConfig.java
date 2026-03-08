package com.claude.learn.config;

import com.claude.learn.agent.AgentTools;
import com.claude.learn.agent.OrchestratorAgent;
import com.claude.learn.agent.PolicyAgent;
import com.claude.learn.agent.SummaryAgent;
import com.claude.learn.service.TokenMonitorService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class LangChainConfig {

    private static final Pattern PG_JDBC_PATTERN = Pattern.compile("^jdbc:postgresql://([^:/?#]+)(?::(\\d+))?/([^?]+).*$");
    private final Logger log = LoggerFactory.getLogger(LangChainConfig.class);

    @Value("${DEEPSEEK_API_KEY}")
    private String apiKey;

    @Value("${EMBEDDING_API_KEY}")
    private String embeddingApiKey;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Bean
    public ChatLanguageModel chatLanguageModel(ChatModelListener chatModelListener) {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(apiKey)
                .modelName("deepseek-chat")
                .listeners(Arrays.asList(chatModelListener))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(embeddingApiKey)
                .modelName("text-embedding-v2")
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        PgJdbcInfo pg = parsePgJdbcUrl(datasourceUrl);
        return PgVectorEmbeddingStore.builder()
                .host(pg.host())
                .port(pg.port())
                .database(pg.database())
                .user(datasourceUsername)
                .password(datasourcePassword)
                .table("rag_embeddings")
                .dimension(1536)
                .createTable(true)
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }

    @Bean
    public PolicyAgent policyAgent(ChatLanguageModel chatLanguageModel, AgentTools agentTools, ChatMemory chatMemory,
                                   ChatModelListener chatModelListener) {
        return AiServices.builder(PolicyAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel(chatModelListener))
                .tools(agentTools)
                .chatMemory(chatMemory)
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel streamingChatLanguageModel(ChatModelListener chatModelListener) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(apiKey)
                .listeners(Arrays.asList(chatModelListener))
                .modelName("deepseek-chat")
                .build();
    }

    @Bean
    public SummaryAgent summaryAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(SummaryAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    @Bean
    public OrchestratorAgent orchestratorAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(OrchestratorAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    @Bean
    public ChatModelListener chatModelListener(TokenMonitorService tokenMonitorService) {
        return new ChatModelListener() {

            @Override
            public void onRequest(ChatModelRequestContext ctx) {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth != null ? auth.getName() : "anonymous";
                ctx.attributes().put("username", username);
                log.info("LLM request sent, user: {}", username);
            }

            @Override
            public void onResponse(ChatModelResponseContext ctx) {
                var tokenUsage = ctx.response().tokenUsage();
                if (tokenUsage != null) {
                    String username = (String) ctx.attributes().getOrDefault("username", "anonymous");
                    tokenMonitorService.record(
                            username,
                            tokenUsage.inputTokenCount(),
                            tokenUsage.outputTokenCount()
                    );
                    log.info("LLM response completed, user: {}, token total: {}", username, tokenUsage.totalTokenCount());
                }
            }

            @Override
            public void onError(ChatModelErrorContext ctx) {
                log.error("LLM call failed: {}", ctx.error().getMessage());
            }
        };
    }

    private PgJdbcInfo parsePgJdbcUrl(String url) {
        Matcher matcher = PG_JDBC_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unsupported PostgreSQL JDBC URL: " + url);
        }

        String host = matcher.group(1);
        int port = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 5432;
        String database = matcher.group(3);
        return new PgJdbcInfo(host, port, database);
    }

    private record PgJdbcInfo(String host, int port, String database) {
    }
}
