package com.example.aiagent.service.llm;

import com.example.aiagent.config.LlmConfig;
import com.example.aiagent.model.dto.llm.ChatCompletionRequest;
import com.example.aiagent.model.dto.llm.ChatCompletionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * DeepSeek API 服务
 * 使用 OpenAI 兼容格式调用 DeepSeek 模型
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekChatService {

    private final LlmConfig llmConfig;

    /**
     * 创建 WebClient 实例
     */
    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(llmConfig.getDeepseek().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + llmConfig.getDeepseek().getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 发送聊天请求到 DeepSeek API（简单版本）
     * @param messages 消息列表
     * @return API 响应
     */
    public ChatCompletionResponse chat(List<ChatCompletionRequest.Message> messages) {
        return chat(messages, null, null);
    }

    /**
     * 发送聊天请求到 DeepSeek API（自定义参数版本）
     * @param messages 消息列表
     * @param temperature 温度参数，控制随机性
     * @param maxTokens 最大生成 token 数
     * @return API 响应
     */
    public ChatCompletionResponse chat(
            List<ChatCompletionRequest.Message> messages,
            Double temperature,
            Integer maxTokens
    ) {
        // 构建请求
        ChatCompletionRequest request = new ChatCompletionRequest(
                llmConfig.getDeepseek().getModel(),
                messages,
                temperature,
                maxTokens,
                null,
                null
        );

        log.info("发送聊天请求到 DeepSeek: model={}", llmConfig.getDeepseek().getModel());

        try {
            // 发送请求并获取响应
            ChatCompletionResponse response = getWebClient()
                    .post()
                    .uri("/v1/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatCompletionResponse.class)
                    .block();

            log.info("收到 DeepSeek 响应: choices={}", 
                    response != null ? response.choices().size() : 0);
            return response;
        } catch (WebClientResponseException e) {
            log.error("DeepSeek API 错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("DeepSeek API 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送带工具调用的聊天请求
     * @param messages 消息列表
     * @param tools 可用工具列表
     * @return API 响应
     */
    public ChatCompletionResponse chatWithTools(
            List<ChatCompletionRequest.Message> messages,
            List<ChatCompletionRequest.Tool> tools
    ) {
        ChatCompletionRequest request = new ChatCompletionRequest(
                llmConfig.getDeepseek().getModel(),
                messages,
                null,
                null,
                null,
                tools
        );

        log.info("发送带工具的聊天请求到 DeepSeek: model={}, tools={}", 
                llmConfig.getDeepseek().getModel(), tools.size());

        try {
            ChatCompletionResponse response = getWebClient()
                    .post()
                    .uri("/v1/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatCompletionResponse.class)
                    .block();

            log.info("收到 DeepSeek 响应: choices={}", 
                    response != null ? response.choices().size() : 0);
            return response;
        } catch (WebClientResponseException e) {
            log.error("DeepSeek API 错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("DeepSeek API 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 流式聊天（未来实现）
     * @param messages 消息列表
     * @param handler 流式处理回调
     */
    public void streamChat(List<ChatCompletionRequest.Message> messages, StreamHandler handler) {
        ChatCompletionRequest request = new ChatCompletionRequest(
                llmConfig.getDeepseek().getModel(),
                messages,
                null,
                null,
                true,
                null
        );

        getWebClient()
                .post()
                .uri("/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(handler::onChunk);
    }

    /**
     * 流式处理回调接口
     */
    @FunctionalInterface
    public interface StreamHandler {
        void onChunk(String chunk);
    }
}
