package com.claude.learn;

import com.claude.learn.domain.DocumentSegment;
import com.claude.learn.repository.DocumentSegmentRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;
import java.util.List;

/**
 * 测试写入向量化数据和全文搜索数据
 */
@SpringBootTest
public class HybridIngestTest {


    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private DocumentSegmentRepository repository;

    @Test
    void ingest() {
        // 加载 PDF
        var path = Paths.get("src/main/resources/docs/policy.pdf");
        Document document = FileSystemDocumentLoader.loadDocument(
                path, new ApachePdfBoxDocumentParser()
        );

        // 分块
        var splitter = new DocumentBySentenceSplitter(300, 30);
        List<TextSegment> segments = splitter.split(document);
        System.out.println("分块数量：" + segments.size());

        // 同时写入两个地方
        for (TextSegment segment : segments) {
            // 1. 写入向量库
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);

            // 2. 写入全文检索表
            repository.save(new DocumentSegment(segment.text(), "policy.pdf"));
        }

        System.out.println("✅ 向量库 + 全文检索表 同时入库完成");
    }

}
