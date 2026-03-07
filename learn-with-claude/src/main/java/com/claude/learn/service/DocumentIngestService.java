package com.claude.learn.service;

import com.claude.learn.config.RagProperties;
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
    private final RagProperties ragProperties;

    public DocumentIngestService(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            DocumentSegmentRepository repository,
            RagProperties ragProperties) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.repository = repository;
        this.ragProperties = ragProperties;
    }

    public void ingest(byte[] fileBytes, String filename) {

        log.info("Start ingest document: {}", filename);
        String docType = resolveDocType(filename);

        Document document;
        try (var inputStream = new ByteArrayInputStream(fileBytes)) {
            if ("pdf".equals(docType)) {
                document = new ApachePdfBoxDocumentParser().parse(inputStream);
            } else if ("docx".equals(docType)) {
                document = new ApachePoiDocumentParser().parse(inputStream);
            } else {
                throw new IllegalArgumentException("Unsupported file type");
            }
        } catch (Exception e) {
            log.error("Parse document failed", e);
            throw new RuntimeException("Parse document failed: " + e.getMessage());
        }

        var splitter = new DocumentBySentenceSplitter(
                ragProperties.getChunk().getMaxSegmentSize(),
                ragProperties.getChunk().getMaxOverlapSize()
        );
        List<TextSegment> segments = splitter.split(document);
        log.info("Document split into {} segments", segments.size());

        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
            repository.save(new DocumentSegment(segment.text(), filename, docType));
        }
        log.info("Document ingest completed: {}, stored {} segments", filename, segments.size());
    }

    private String resolveDocType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
