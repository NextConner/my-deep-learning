package com.claude.learn.service;

import org.springframework.stereotype.Service;
import com.claude.learn.domain.DocumentSegment;
import com.claude.learn.repository.DocumentSegmentRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
public class DocumentIngestService {


    private static final Logger log = LoggerFactory.getLogger(DocumentIngestService.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DocumentSegmentRepository repository;

    public DocumentIngestService(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            DocumentSegmentRepository repository) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.repository = repository;
    }

    //解析文档
    public void ingest(byte[] fileBytes,String filename){

        log.info("📄 开始处理文档：{}", filename);

        //区分类型
        Document document ;
        try(var inputStream = new ByteArrayInputStream(fileBytes)) {
            if (filename.endsWith(".pdf")) {
                document = new ApachePdfBoxDocumentParser().parse(inputStream);
            } else if (filename.endsWith(".docx")) {
                document = new ApachePoiDocumentParser().parse(inputStream);
            } else {
                throw new IllegalArgumentException("不支持的文件类型");
            }
        } catch (Exception e) {
            log.error("❌ 解析文档失败：", e);
            throw new RuntimeException("解析文档失败: " + e.getMessage());
        }

        //分块
        var splitter = new DocumentBySentenceSplitter(300,30);
        List<TextSegment> segments = splitter.split(document);
        log.info("✂️ 文档分割成 {} 个片段", segments.size());

        //写入向量库和全文检索
        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding,segment);
            repository.save(new DocumentSegment(segment.text(),filename));
        }
        log.info("✅ 文档处理完成:{}，已存储 {} 个片段",filename ,segments.size());
    }


}
