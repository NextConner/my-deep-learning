package com.example.aiagent.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG（检索增强生成）服务
 * 负责从向量数据库检索相关上下文并构建增强提示词
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStoreService vectorStoreService;

    /**
     * 检索相关上下文
     * @param tenantId 租户ID
     * @param query 用户查询
     * @param topK 返回前 K 个最相关的结果
     * @return 格式化的上下文字符串
     */
    public String retrieveContext(String tenantId, String query, int topK) {
        log.info("为租户 {} 检索上下文: query={}", tenantId, query);
        
        // 1. 从向量数据库搜索相似内容
        List<VectorStoreService.SearchResult> results = vectorStoreService.search(tenantId, query, topK);
        
        // 2. 如果没有找到相关结果，返回空字符串
        if (results.isEmpty()) {
            log.info("未找到相关上下文");
            return "";
        }
        
        // 3. 格式化构建上下文字符串
        StringBuilder context = new StringBuilder();
        context.append("相关上下文:\n\n");
        
        for (int i = 0; i < results.size(); i++) {
            VectorStoreService.SearchResult r = results.get(i);
            context.append(String.format("[%d] %s (相似度: %.2f)\n%s\n\n",
                    i + 1, r.documentTitle(), r.similarity(), r.content()));
        }
        
        log.info("检索到 {} 个相关文档块", results.size());
        return context.toString();
    }

    /**
     * 构建增强提示词
     * 将检索到的上下文与用户问题结合，生成用于 LLM 的提示词
     * @param query 用户问题
     * @param context 检索到的上下文（可选）
     * @return 增强后的提示词
     */
    public String buildAugmentedPrompt(String query, String context) {
        // 如果没有上下文，直接返回用户问题
        if (context == null || context.isEmpty()) {
            return "用户问题: " + query;
        }
        
        // 构建包含上下文的增强提示词
        return """
                请根据以下上下文回答问题。如果上下文没有相关信息，请如实说明。
                
                %s
                
                用户问题: %s
                """.formatted(context, query);
    }
}
