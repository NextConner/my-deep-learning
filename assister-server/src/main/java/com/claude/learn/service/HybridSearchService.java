package com.claude.learn.service;

import com.claude.learn.domain.DocumentSegment;
import com.claude.learn.repository.DocumentSegmentRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HybridSearchService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DocumentSegmentRepository repository;

    public HybridSearchService(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            DocumentSegmentRepository repository) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.repository = repository;
    }

    public List<String> hybridSearch(String query, int topK) {
        return hybridSearch(query, topK, null);
    }

    public List<String> hybridSearch(String query, int topK, HybridSearchFilter filter) {

        String source = filter != null ? filter.source() : null;
        var fromTime = filter != null ? filter.fromTime() : null;

        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .minScore(0.5)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> vectorMatches = searchResult.matches();

        List<DocumentSegment> keywordMatches = repository.fullTextSearchWithFilters(query, topK, source, fromTime);

        Map<String, Double> scoreMap = new LinkedHashMap<>();

        for (int i = 0; i < vectorMatches.size(); i++) {
            EmbeddingMatch<TextSegment> match = vectorMatches.get(i);
            String content = match.embedded().text();

            if (isFilteredOut(content, source, fromTime)) {
                continue;
            }

            double rrfScore = 1.0 / (60 + i + 1);
            scoreMap.merge(content, rrfScore, Double::sum);
        }

        for (int i = 0; i < keywordMatches.size(); i++) {
            String content = keywordMatches.get(i).getContent();
            double rrfScore = 1.0 / (60 + i + 1);
            scoreMap.merge(content, rrfScore, Double::sum);
        }

        return scoreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .toList();
    }

    private boolean isFilteredOut(String content, String source, java.time.LocalDateTime fromTime) {
        if (source == null && fromTime == null) {
            return false;
        }
        return repository.countByContentWithFilters(content, source, fromTime) == 0;
    }
}
