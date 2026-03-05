package com.example.aiagent.service.llm;

import com.example.aiagent.config.LlmConfig;
import com.example.aiagent.model.dto.llm.ChatCompletionRequest;
import com.example.aiagent.model.dto.llm.ChatCompletionResponse;
import com.example.aiagent.model.dto.llm.EmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * LLM 统一网关服务
 * 路由请求到不同的 LLM 提供商（DeepSeek、OpenAI 等）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmGatewayService {

    private final LlmConfig llmConfig;
    private final DeepSeekChatService deepSeekChatService;
    private final BailianEmbeddingService bailianEmbeddingService;

    /**
     * 发送聊天消息并获取响应（简单版本）
     * @param message 用户消息
     * @return API 响应
     */
    public ChatCompletionResponse chat(String message) {
        return chat(List.of(new ChatCompletionRequest.Message("user", message, null, null)));
    }

    /**
     * 发送聊天消息列表并获取响应
     * @param messages 消息列表
     * @return API 响应
     */
    public ChatCompletionResponse chat(List<ChatCompletionRequest.Message> messages) {
        String provider = llmConfig.getProvider().toLowerCase();

        log.info("LLM 网关路由到提供商: {}", provider);

        return switch (provider) {
            case "deepseek" -> deepSeekChatService.chat(messages);
            // 未来可添加: OpenAI, Anthropic 等
            default -> throw new UnsupportedOperationException("不支持的 LLM 提供商: " + provider);
        };
    }

    /**
     * 发送带工具的聊天请求
     * @param messages 消息列表
     * @param tools 可用工具列表
     * @return API 响应
     */
    public ChatCompletionResponse chatWithTools(
            List<ChatCompletionRequest.Message> messages,
            List<ChatCompletionRequest.Tool> tools
    ) {
        String provider = llmConfig.getProvider().toLowerCase();

        log.info("LLM 网关路由到提供商: {} (带工具)", provider);

        return switch (provider) {
            case "deepseek" -> deepSeekChatService.chatWithTools(messages, tools);
            default -> throw new UnsupportedOperationException("不支持的 LLM 提供商: " + provider);
        };
    }

    /**
     * 为文本生成 embedding 向量
     * @param text 输入文本
     * @return embedding 向量
     */
    public List<Float> embed(String text) {
        return bailianEmbeddingService.embed(text);
    }

    /**
     * 批量生成多个文本的 embedding 向量
     * @param texts 输入文本列表
     * @return embedding 向量列表
     */
    public List<List<Float>> embedBatch(List<String> texts) {
        return bailianEmbeddingService.embedBatch(texts).stream()
                .map(EmbeddingResponse.EmbeddingItem::embedding)
                .toList();
    }

    /**
     * 获取当前配置的 LLM 提供商名称
     * @return 提供商名称
     */
    public String getProvider() {
        return llmConfig.getProvider();
    }
}
