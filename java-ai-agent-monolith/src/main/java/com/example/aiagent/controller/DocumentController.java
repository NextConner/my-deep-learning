package com.example.aiagent.controller;

import com.example.aiagent.model.dto.DocumentIngestRequest;
import com.example.aiagent.model.dto.DocumentIngestResponse;
import com.example.aiagent.service.document.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档控制器
 * 处理文档上传和摄入
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 文档摄入接口
     * 接收文档内容，自动分块并生成向量嵌入
     */
    @PostMapping(value = "/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentIngestResponse ingest(@Valid @RequestBody DocumentIngestRequest request) {
        log.info("文档摄入: tenant={}, title={}", request.tenantId(), request.title());
        
        try {
            // 调用文档服务进行摄入
            DocumentService.IngestResult result = documentService.ingest(
                    request.tenantId(),
                    request.title(),
                    request.content(),
                    request.source()
            );
            return new DocumentIngestResponse(result.documentId(), result.chunkCount());
        } catch (Exception e) {
            log.error("文档摄入错误: {}", e.getMessage(), e);
            throw new RuntimeException("文档摄入失败: " + e.getMessage(), e);
        }
    }
}
