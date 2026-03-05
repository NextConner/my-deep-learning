package com.example.aiagent.service.tool;

import com.example.aiagent.service.rag.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 文档搜索工具
 * 使用 RAG 服务搜索上传的文档
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchDocumentsTool implements Tool {

    private final RagService ragService;

    @Override
    public String getName() {
        return "search_documents";
    }

    @Override
    public String getDescription() {
        return "搜索上传的文档以找到相关信息。当用户询问特定文档、政策或信息时使用此工具。";
    }

    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "query", Map.of(
                                "type", "string",
                                "description", "用于搜索文档的查询语句"
                        ),
                        "tenant_id", Map.of(
                                "type", "string", 
                                "description", "要搜索的租户ID"
                        )
                ),
                "required", java.util.List.of("query", "tenant_id")
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        String query = (String) arguments.get("query");
        String tenantId = (String) arguments.get("tenant_id");
        
        log.info("执行文档搜索: tenant={}, query={}", tenantId, query);
        
        try {
            // 调用 RAG 服务检索相关上下文
            String context = ragService.retrieveContext(tenantId, query, 5);
            if (context.isEmpty()) {
                return ToolResult.ok("未找到与查询相关的文档: " + query);
            }
            return ToolResult.ok(context);
        } catch (Exception e) {
            log.error("搜索错误: {}", e.getMessage());
            return ToolResult.error("搜索失败: " + e.getMessage());
        }
    }
}
