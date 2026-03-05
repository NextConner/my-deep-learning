package com.example.aiagent.controller;

import com.example.aiagent.model.dto.ChatRequest;
import com.example.aiagent.model.dto.ChatResponse;
import com.example.aiagent.model.dto.CitationDto;
import com.example.aiagent.model.dto.UsageDto;
import com.example.aiagent.model.dto.llm.ChatCompletionRequest;
import com.example.aiagent.model.dto.llm.ChatCompletionResponse;
import com.example.aiagent.service.llm.LlmGatewayService;
import com.example.aiagent.service.rag.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天控制器
 * 处理用户的聊天请求，集成 RAG 检索增强功能
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final LlmGatewayService llmGatewayService;
    private final RagService ragService;

    /**
     * 聊天接口
     * 处理用户消息，进行 RAG 检索增强后调用 LLM
     */
    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        log.info("收到聊天请求: tenant={}, user={}", request.tenantId(), request.userId());

        try {
            // 步骤 1: 从 RAG 检索相关上下文
            String context = ragService.retrieveContext(request.tenantId(), request.message(), 3);
            
            // 步骤 2: 构建增强提示词
            String augmentedPrompt = ragService.buildAugmentedPrompt(request.message(), context);
            
            // 步骤 3: 调用 LLM
            List<ChatCompletionRequest.Message> messages = List.of(
                    new ChatCompletionRequest.Message("user", augmentedPrompt, null, null)
            );
            
            ChatCompletionResponse response = llmGatewayService.chat(messages);

            // 步骤 4: 提取答案
            String answer = response.choices() != null && !response.choices().isEmpty()
                    ? response.choices().get(0).message().content()
                    : "LLM 未返回响应";

            // 步骤 5: 构建引用信息
            List<CitationDto> citations = new ArrayList<>();
            if (!context.isEmpty()) {
                // 简单解析上下文构建引用
                String[] chunks = context.split("\\[\\d+\\]");
                for (int i = 1; i < chunks.length; i++) {
                    citations.add(new CitationDto((long) i, (long) i, "context", chunks[i].trim()));
                }
            }

            // 步骤 6: 提取使用统计
            int promptTokens = response.usage() != null ? response.usage().promptTokens() : 0;
            int completionTokens = response.usage() != null ? response.usage().completionTokens() : 0;

            return new ChatResponse(
                    answer,
                    citations,
                    new UsageDto(
                            llmGatewayService.getProvider(),
                            response.model(),
                            promptTokens,
                            completionTokens,
                            null
                    )
            );
        } catch (Exception e) {
            log.error("聊天错误: {}", e.getMessage(), e);
            return new ChatResponse(
                    "错误: " + e.getMessage(),
                    List.of(),
                    new UsageDto("error", "none", 0, 0, null)
            );
        }
    }
}
