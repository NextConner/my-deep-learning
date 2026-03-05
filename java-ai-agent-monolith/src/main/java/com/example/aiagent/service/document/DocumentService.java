package com.example.aiagent.service.document;

import com.example.aiagent.service.llm.BailianEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档处理服务
 * 负责文档的自动分块、embedding 生成和存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final JdbcTemplate jdbcTemplate;
    private final BailianEmbeddingService embeddingService;

    /** 每个文档块的最大字符数 */
    private static final int CHUNK_SIZE = 500;
    /** 文档块之间的重叠字符数 */
    private static final int CHUNK_OVERLAP = 50;

    /**
     * 摄入文档（自动分块和生成 embedding）
     * @param tenantId 租户ID
     * @param title 文档标题
     * @param content 文档内容
     * @param source 文档来源
     * @return 摄入结果（文档ID和块数量）
     */
    @Transactional
    public IngestResult ingest(String tenantId, String title, String content, String source) {
        log.info("摄入文档: tenant={}, title={}", tenantId, title);
        
        // 步骤 1: 插入文档记录
        Long docId = insertDocument(tenantId, title, source);
        
        // 步骤 2: 对文档内容进行分块
        List<String> chunks = chunkText(content);
        log.info("将内容分成 {} 个块", chunks.size());
        
        // 步骤 3: 存储文档块和对应的 embedding
        int storedChunks = 0;
        for (int i = 0; i < chunks.size(); i++) {
            String chunkContent = chunks.get(i);
            
            // 插入文档块
            Long chunkId = insertChunk(tenantId, docId, i, chunkContent);
            
            // 生成并存储 embedding
            storeEmbedding(tenantId, docId, chunkId, chunkContent);
            storedChunks++;
        }
        
        log.info("文档 {} 摄入完成，包含 {} 个块", docId, storedChunks);
        return new IngestResult(docId, storedChunks);
    }

    /**
     * 文本分块（基于字符数，支持重叠）
     * 尝试在句子边界处分割，保持语义完整性
     * @param content 原始文本内容
     * @return 分块后的文本列表
     */
    private List<String> chunkText(String content) {
        List<String> chunks = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return chunks;
        }
        
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + CHUNK_SIZE, content.length());
            
            // 尝试在句子边界处分割
            if (end < content.length()) {
                int lastPeriod = content.lastIndexOf('.', end);
                int lastNewline = content.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastPeriod, lastNewline);
                if (breakPoint > start + CHUNK_SIZE / 2) {
                    end = breakPoint + 1;
                }
            }
            
            chunks.add(content.substring(start, end).trim());
            start = end - CHUNK_OVERLAP;
            if (start <= chunks.get(chunks.size() - 1).length()) {
                start = end;
            }
        }
        
        return chunks;
    }

    /**
     * 插入文档记录
     */
    private Long insertDocument(String tenantId, String title, String source) {
        String sql = """
            INSERT INTO document (tenant_id, title, source)
            VALUES (?, ?, ?)
            RETURNING id
            """;
        
        return jdbcTemplate.queryForObject(sql, Long.class, tenantId, title, source);
    }

    /**
     * 插入文档块记录
     */
    private Long insertChunk(String tenantId, Long docId, int index, String content) {
        String sql = """
            INSERT INTO document_chunk (tenant_id, document_id, chunk_index, content)
            VALUES (?, ?, ?, ?)
            RETURNING id
            """;
        
        return jdbcTemplate.queryForObject(sql, Long.class, tenantId, docId, index, content);
    }

    /**
     * 存储 embedding 向量
     */
    private void storeEmbedding(String tenantId, Long docId, Long chunkId, String content) {
        List<Float> embedding = embeddingService.embed(content);
        String embeddingStr = toPgArray(embedding);
        
        String sql = """
            INSERT INTO chunk_embedding (tenant_id, document_id, chunk_id, embedding)
            VALUES (?, ?, ?, ?::vector)
            ON CONFLICT (tenant_id, chunk_id) 
            DO UPDATE SET embedding = EXCLUDED.embedding
            """;
        
        jdbcTemplate.update(sql, tenantId, docId, chunkId, embeddingStr);
    }

    /**
     * 将 List<Float> 转换为 PostgreSQL 数组格式
     */
    private String toPgArray(List<Float> embedding) {
        return "[" + embedding.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("") + "]";
    }

    /**
     * 文档摄入结果
     * @param documentId 文档ID
     * @param chunkCount 文档块数量
     */
    public record IngestResult(Long documentId, int chunkCount) {}
}
