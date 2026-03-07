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

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class LangChainConfig {

    private Logger log = LoggerFactory.getLogger(LangChainConfig.class);

    @Value("${DEEPSEEK_API_KEY}")
    private String apiKey;

    @Value("${EMBEDDING_API_KEY}")
    private String embeddingApiKey;


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
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource) {
        return PgVectorEmbeddingStore.builder()
                .host("192.168.20.129")
                .port(5432)
                .database("ragdb")
                .user("pgsql")
                .password("123456")
                .table("rag_embeddings")
                .dimension(1536)
                .createTable(true)
                .build();
    }


    /**
     * LangChain4j 的 ChatMemory 用于在对话过程中保留上下文信息，增强模型的连续对话能力。
     * @return
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10); // 保留最近10条对话
    }

    /**
     * 注入自定义 agent
     */
    @Bean
    public PolicyAgent policyAgent(ChatLanguageModel chatLanguageModel, AgentTools agentTools, ChatMemory chatMemory
    ,ChatModelListener chatModelListener) {
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

    /**
     * 注册agent 汇总bean
     * @param chatLanguageModel
     * @return
     */
    @Bean
    public SummaryAgent summaryAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(SummaryAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    /**
     * 任务编排bean
     * @param chatLanguageModel
     * @return
     */
    @Bean
    public OrchestratorAgent orchestratorAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(OrchestratorAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }


    /**
     * 可观测性
     */
    @Bean
    public ChatModelListener chatModelListener(TokenMonitorService tokenMonitorService) {
        return new ChatModelListener() {

            @Override
            public void onRequest(ChatModelRequestContext ctx) {
                // 这里还在主线程，能拿到 SecurityContext
                var auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth != null ? auth.getName() : "anonymous";
                ctx.attributes().put("username", username);  // ← 存入请求属性
                log.info("📤 LLM 请求发出，用户：{}", username);
            }

            @Override
            public void onResponse(ChatModelResponseContext ctx) {
                var tokenUsage = ctx.response().tokenUsage();
                if (tokenUsage != null) {
                    var auth = SecurityContextHolder.getContext().getAuthentication();
                    String username = (String) ctx.attributes().getOrDefault("username", "anonymous");
                    tokenMonitorService.record(
                            username,
                            tokenUsage.inputTokenCount(),
                            tokenUsage.outputTokenCount()
                    );
                    log.info("📥 LLM 响应完成，用户 {}，Token 消耗：{}", username, tokenUsage.totalTokenCount());
                }
            }

            @Override
            public void onError(ChatModelErrorContext ctx) {
                log.error("❌ LLM 调用出错：{}", ctx.error().getMessage());
            }

            private String getUsernameFromContext() {
                // 从 Spring Security 上下文获取当前用户
                var auth = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
                return auth != null ? auth.getName() : "anonymous";
            }
        };
    }
}
