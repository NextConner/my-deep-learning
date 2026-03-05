package com.example.aiagent.service.llm;

import com.example.aiagent.config.LlmConfig;
import com.example.aiagent.model.dto.llm.EmbeddingRequest;
import com.example.aiagent.model.dto.llm.EmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * 阿里百炼 Embedding 服务
 * 调用百炼 API 生成文本向量嵌入
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BailianEmbeddingService {

    private final LlmConfig llmConfig;

    /**
     * 创建 WebClient 实例
     */
    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(llmConfig.getBailian().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + llmConfig.getBailian().getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 为单个文本生成 embedding 向量
     * @param text 输入文本
     * @return embedding 向量（List<Float>）
     */
    public List<Float> embed(String text) {
        List<EmbeddingResponse.EmbeddingItem> items = embedBatch(List.of(text));
        if (items.isEmpty()) {
            throw new RuntimeException("No embedding returned");
        }
        return items.get(0).embedding();
    }

    /**
     * 批量生成多个文本的 embedding 向量
     * @param texts 输入文本列表
     * @return embedding 结果列表
     */
    public List<EmbeddingResponse.EmbeddingItem> embedBatch(List<String> texts) {
        if (texts.isEmpty()) {
            return List.of();
        }

        // 构建请求
        EmbeddingRequest request = new EmbeddingRequest(
                llmConfig.getBailian().getEmbeddingModel(),
                texts
        );

        log.info("发送 embedding 请求到百炼: model={}, batch_size={}", 
                llmConfig.getBailian().getEmbeddingModel(), texts.size());

        try {
            // 发送请求
            EmbeddingResponse response = getWebClient()
                    .post()
                    .uri("/api/v2/services/embedding/text-embedding")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmbeddingResponse.class)
                    .block();

            if (response == null || response.output() == null) {
                throw new RuntimeException("Empty embedding response");
            }

            log.info("收到百炼 embedding 响应: count={}", 
                    response.output().embeddings().size());
            return response.output().embeddings();
        } catch (WebClientResponseException e) {
            log.error("百炼 API 错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("百炼 Embedding API 调用失败: " + e.getMessage(), e);
        }
    }
}
