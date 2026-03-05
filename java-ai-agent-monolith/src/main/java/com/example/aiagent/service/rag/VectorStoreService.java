package com.example.aiagent.service.rag;

import com.example.aiagent.config.LlmConfig;
import com.example.aiagent.service.llm.BailianEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 向量存储服务
 * 使用 pgvector 实现向量相似度搜索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final JdbcTemplate jdbcTemplate;
    private final BailianEmbeddingService embeddingService;
    private final LlmConfig llmConfig;

    /**
     * 搜索相似的文档块
     * @param tenantId 租户ID
     * @param query 查询文本
     * @param topK 返回前 K 个最相似的结果
     * @return 搜索结果列表
     */
    public List<SearchResult> search(String tenantId, String query, int topK) {
        // 1. 为查询文本生成 embedding 向量
        List<Float> queryEmbedding = embeddingService.embed(query);
        
        // 2. 使用向量相似度搜索（余弦相似度 = 1 - 余弦距离）
        String sql = """
            SELECT dc.id, dc.content, dc.document_id, d.title,
                   1 - (ce.embedding <=> ?) AS similarity
            FROM chunk_embedding ce
            JOIN document_chunk dc ON ce.chunk_id = dc.id
            JOIN document d ON dc.document_id = d.id
            WHERE ce.tenant_id = ?
            ORDER BY ce.embedding <=> ?
            LIMIT ?
            """;
        
        // 3. 将 List<Float> 转换为 PostgreSQL 数组格式
        String embeddingStr = toPgArray(queryEmbedding);
        
        // 4. 执行查询
        return jdbcTemplate.query(sql, (rs, rowNum) -> new SearchResult(
                rs.getLong("id"),
                rs.getString("content"),
                rs.getLong("document_id"),
                rs.getString("title"),
                rs.getDouble("similarity")
        ), embeddingStr, tenantId, embeddingStr, topK);
    }

    /**
     * 为文档块存储 embedding 向量
     * @param tenantId 租户ID
     * @param chunkId 文档块ID
     * @param content 文档块内容
     */
    public void storeEmbedding(Long tenantId, Long chunkId, String content) {
        // 1. 生成 embedding 向量
        List<Float> embedding = embeddingService.embed(content);
        String embeddingStr = toPgArray(embedding);
        
        // 2. 存储到数据库（如果已存在则更新）
        String sql = """
            INSERT INTO chunk_embedding (tenant_id, chunk_id, embedding)
            VALUES (?, ?, ?::vector)
            ON CONFLICT (tenant_id, chunk_id) 
            DO UPDATE SET embedding = EXCLUDED.embedding
            """;
        
        jdbcTemplate.update(sql, tenantId, chunkId, embeddingStr);
        log.info("为租户 {} 的块 {} 存储 embedding", tenantId, chunkId);
    }

    /**
     * 将 List<Float> 转换为 PostgreSQL 数组字符串格式
     * @param embedding embedding 向量
     * @return PostgreSQL 数组格式字符串
     */
    private String toPgArray(List<Float> embedding) {
        return "[" + embedding.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("") + "]";
    }

    /**
     * 搜索结果记录
     * @param chunkId 文档块ID
     * @param content 文档块内容
     * @param documentId 文档ID
     * @param documentTitle 文档标题
     * @param similarity 相似度分数
     */
    public record SearchResult(
            Long chunkId,
            String content,
            Long documentId,
            String documentTitle,
            Double similarity
    ) {}
}
