# RAG 单元 — Overview

## 单元摘要

RAG（检索增强生成）子系统，负责文档上传/解析/切分/向量化入库，以及混合检索（向量相似度 + PostgreSQL 全文检索）为 Agent 提供知识来源。

## 需求背景

企业内部政策文档（PDF/DOCX）需要被上传并建立索引，用户提问时从文档中召回相关片段供 LLM 参考生成答案。

## 单元目标

1. 文档上传与解析（PDF/DOCX）
2. 分段（sentence splitter）与向量化（EmbeddingModel）
3. 双路写入：pgvector（向量索引）+ PostgreSQL（全文检索）
4. 混合检索（RRF 倒排排名融合），返回 top-K 片段

## 关键代码

| 文件 | 行号 | 说明 |
|------|------|------|
| `controller/DocumentController.java` | `POST /api/documents/upload` 入口 |
| `service/DocumentIngestService.java:42` | `ingest(fileBytes, filename)` 主入库方法 |
| `service/HybridSearchService.java:34` | `hybridSearch(query, topK)` 混合检索 |
| `service/HybridSearchService.java:38` | 带过滤器版本 `hybridSearch(query, topK, filter)` |
| `domain/DocumentSegment.java` | 文档片段实体 |
| `config/RagProperties.java` | `rag.chunk.*` 配置 |
| `config/LangChainConfig.java` | EmbeddingStore 配置（**注意：DB URL 硬编码** `192.168.20.129:5432/ragdb`）|

## 入口与边界

- **写入入口**：`POST /api/documents/upload`（multipart/form-data，支持 PDF/DOCX）
- **读取出口**：`AgentTools.searchPolicy(query)` → `HybridSearchService.hybridSearch(query, 3)`
- **限制**：仅支持 PDF 和 DOCX，其他类型抛出 `IllegalArgumentException`

## 文档入库流程

```
上传文件（bytes + filename）
  → 检测 docType（pdf / docx）
  → ApachePdfBoxDocumentParser / ApachePoiDocumentParser 解析
  → DocumentBySentenceSplitter.split()
      maxSegmentSize: 300 tokens
      maxOverlapSize: 30 tokens
  → 每个 TextSegment：
      ├─ EmbeddingModel.embed(segment) → pgvector（embeddingStore.add）
      └─ repository.save(DocumentSegment{content, filename, docType})
```

## 混合检索（hybridSearch）

算法：RRF（Reciprocal Rank Fusion），融合两路排名

```
输入：query, topK, filter(source, fromTime)

路径A — 向量检索：
  EmbeddingModel.embed(query) → EmbeddingStore.search(minScore=0.5, maxResults=topK)
  → vectorMatches（按相似度排序）

路径B — 全文检索：
  repository.fullTextSearchWithFilters(query, topK, source, fromTime)
  → keywordMatches（PostgreSQL tsvector）

RRF 融合：
  score(content) += 1 / (60 + rank + 1)  （两路分别计算后累加）
  → 按 score 降序取 top-K
```

## 过滤器（HybridSearchFilter）

```java
record HybridSearchFilter(String source, LocalDateTime fromTime)
```
- `source`：按文件名过滤
- `fromTime`：按上传时间过滤（向量路径由 `countByContentWithFilters` 二次验证）

## 数据模型

`domain/DocumentSegment.java`：
- `content`：文本片段
- `filename`：原始文件名（即 source）
- `docType`：`pdf` / `docx`
- `createdAt`：入库时间

## 风险与未知项

- **[Author's analysis]** `LangChainConfig.java` 中 EmbeddingStore 的 DB URL 硬编码为 `192.168.20.129:5432/ragdb`，需替换为环境变量
- 向量检索与全文检索结果在 `isFilteredOut` 中的过滤逻辑通过额外 SQL `countByContentWithFilters` 实现，N+1 风险
- 文档不支持去重，同一文件多次上传会产生重复片段
