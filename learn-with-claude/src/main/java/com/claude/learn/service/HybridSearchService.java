package com.claude.learn.service;


import com.claude.learn.domain.DocumentSegment;
import com.claude.learn.repository.DocumentRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.*;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HybridSearchService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DocumentRepository repository;

    public HybridSearchService(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            DocumentRepository repository) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.repository = repository;
    }

    public List<String> hybridSearch(String query, int topK){
        return hybridSearch(query,topK,null);
    }

    public List<String> hybridSearch(String query, int topK, HybridSearchFilter filter) {

        String source = filter != null ? filter.source() : null;
        var fromTime = filter != null ? filter.fromTime() : null;

        // 1 query embedding
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 2 vector search
        List<EmbeddingMatch<TextSegment>> vectorMatches = vectorSearch(queryEmbedding, 20);

        // 3 keyword search
        List<DocumentSegment> keywordMatches =
                repository.fullTextSearchWithFilters(query, 20, source, fromTime);

        // 4 RRF merge
        Map<String, Double> scoreMap =
                rrfMerge(vectorMatches, keywordMatches, source, fromTime);

        // 5 deduplicate
        List<Map.Entry<String, Double>> dedupResults =
                deduplicate(scoreMap);

        // 6 rerank
        List<Map.Entry<String, Double>> reranked =
                rerank(queryEmbedding, dedupResults);

        return reranked.stream()
                .limit(topK)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<EmbeddingMatch<TextSegment>> vectorSearch(
            Embedding queryEmbedding,
            int topK) {

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .minScore(0.5)
                .build();

        EmbeddingSearchResult<TextSegment> result =
                embeddingStore.search(request);

        return result.matches();
    }

    private Map<String, Double> rrfMerge(
            List<EmbeddingMatch<TextSegment>> vectorMatches,
            List<DocumentSegment> keywordMatches,
            String source,
            Object fromTime) {

        Map<String, Double> scoreMap = new LinkedHashMap<>();

        // vector RRF
        for (int i = 0; i < vectorMatches.size(); i++) {

            String content =
                    vectorMatches.get(i).embedded().text();

            if (isFilteredOut(content, source, fromTime)) {
                continue;
            }

            content = limitChunkLength(content);

            double score = rrfScore(i);

            scoreMap.merge(content, score, Double::sum);
        }

        // keyword RRF
        for (int i = 0; i < keywordMatches.size(); i++) {

            String content = keywordMatches.get(i).getContent();

            content = limitChunkLength(content);

            double score = rrfScore(i);

            scoreMap.merge(content, score, Double::sum);
        }

        return scoreMap;
    }

    private double rrfScore(int rank) {
        return 1.0 / (60 + rank + 1);
    }

    private String limitChunkLength(String text) {

        int maxLength = 800;

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength);
    }

    private boolean isFilteredOut(
            String content,
            String source,
            Object fromTime) {

        if (source == null && fromTime == null) {
            return false;
        }

        return false;
    }

    private List<Map.Entry<String, Double>> deduplicate(
            Map<String, Double> scoreMap) {

        Set<Integer> seen = new HashSet<>();

        List<Map.Entry<String, Double>> result =
                new ArrayList<>();

        for (var entry : scoreMap.entrySet()) {

            int hash = entry.getKey().hashCode();

            if (seen.contains(hash)) {
                continue;
            }

            seen.add(hash);

            result.add(entry);
        }

        return result;
    }

    private List<Map.Entry<String, Double>> rerank(
            Embedding queryEmbedding,
            List<Map.Entry<String, Double>> docs) {

        docs.sort((a, b) -> {

            double scoreA =
                    similarity(queryEmbedding, a.getKey());

            double scoreB =
                    similarity(queryEmbedding, b.getKey());

            return Double.compare(scoreB, scoreA);
        });

        return docs;
    }

    private double similarity(
            Embedding queryEmbedding,
            String document) {

        Embedding docEmbedding =
                embeddingModel.embed(document).content();

        return cosineSimilarity(
                queryEmbedding.vector(),
                docEmbedding.vector());
    }

    private double cosineSimilarity(
            float[] v1,
            float[] v2) {

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < v1.length; i++) {

            dot += v1[i] * v2[i];

            normA += v1[i] * v1[i];

            normB += v2[i] * v2[i];
        }

        return dot /
                (Math.sqrt(normA) * Math.sqrt(normB));
    }
}