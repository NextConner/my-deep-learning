package com.claude.learn.service;


import com.claude.learn.domain.DocumentSegment;
import com.claude.learn.domain.ScoredDocument;
import com.claude.learn.repository.DocumentRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public List<String> hybridSearch(String query, int topK) {

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        List<EmbeddingMatch<TextSegment>> vectorMatches =
                vectorSearch(queryEmbedding, 20);

        List<DocumentSegment> keywordMatches =
                repository.fullTextSearch(query, 20);

        Map<String, Double> fused =
                weightedRrf(vectorMatches, keywordMatches);

        List<String> dedupDocs =
                deduplicate(fused);

        // 预计算文档 embedding
        List<ScoredDocument> reranked = precomputeEmbeddings(queryEmbedding, dedupDocs);

        // MMR 多样性选择
        List<String> diversified = mmrDiversification(reranked, topK);

        return diversified;
    }

    private List<EmbeddingMatch<TextSegment>> vectorSearch(Embedding queryEmbedding, int topK) {
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .minScore(0.5)
                .build();
        return embeddingStore.search(request).matches();
    }

    private Map<String, Double> weightedRrf(List<EmbeddingMatch<TextSegment>> vectorMatches,
                                            List<DocumentSegment> keywordMatches) {
        Map<String, Double> scoreMap = new HashMap<>();
        double vectorWeight = 1.2;
        double keywordWeight = 1.0;

        for (int i = 0; i < vectorMatches.size(); i++) {
            String content = vectorMatches.get(i).embedded().text();
            content = limitChunkLength(content);
            double score = vectorWeight * (1.0 / (60 + i + 1));
            scoreMap.merge(content, score, Double::sum);
        }

        for (int i = 0; i < keywordMatches.size(); i++) {
            String content = keywordMatches.get(i).getContent();
            content = limitChunkLength(content);
            double score = keywordWeight * (1.0 / (60 + i + 1));
            scoreMap.merge(content, score, Double::sum);
        }

        return scoreMap;
    }

    private String limitChunkLength(String text) {
        int maxLength = 800;
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private List<String> deduplicate(Map<String, Double> scoreMap) {
        Set<Integer> seen = new HashSet<>();
        List<String> result = new ArrayList<>();
        for (String doc : scoreMap.keySet()) {
            int hash = doc.hashCode();
            if (seen.contains(hash)) continue;
            seen.add(hash);
            result.add(doc);
        }
        return result;
    }

    // 预计算 embedding + rerank
    private List<ScoredDocument> precomputeEmbeddings(Embedding queryEmbedding, List<String> docs) {
        List<ScoredDocument> scored = new ArrayList<>();
        for (String doc : docs) {
            Embedding docEmbedding = embeddingModel.embed(doc).content();
            double score = cosineSimilarity(queryEmbedding.vector(), docEmbedding.vector());
            scored.add(new ScoredDocument(doc, score, docEmbedding.vector()));
        }
        scored.sort((a, b) -> Double.compare(b.score(), a.score()));
        return scored;
    }

    // MMR 多样性选择，直接用预计算 embedding 避免重复调用模型
    private List<String> mmrDiversification(List<ScoredDocument> docs, int topK) {
        List<String> selected = new ArrayList<>();
        double lambda = 0.7;

        while (selected.size() < topK && !docs.isEmpty()) {
            ScoredDocument best = null;
            double bestScore = -Double.MAX_VALUE;

            for (ScoredDocument doc : docs) {
                double relevance = doc.score();
                double diversity = 0.0;

                for (String sel : selected) {
                    float[] selEmbedding = docs.stream()
                            .filter(d -> d.text().equals(sel))
                            .findFirst()
                            .map(ScoredDocument::embedding)
                            .orElse(null);
                    if (selEmbedding != null) {
                        diversity = Math.max(diversity, cosineSimilarity(doc.embedding(), selEmbedding));
                    }
                }

                double mmrScore = lambda * relevance - (1 - lambda) * diversity;
                if (mmrScore > bestScore) {
                    bestScore = mmrScore;
                    best = doc;
                }
            }

            selected.add(best.text());
            docs.remove(best);
        }

        return selected;
    }

    private double cosineSimilarity(float[] v1, float[] v2) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // 支持 context 压缩
    public String compressContext(List<String> docs, ChatLanguageModel model) {
        String joined = String.join("\n", docs);
        String prompt = """
                Extract key facts from the following text.
                Return concise bullet points.
                
                %s
                """.formatted(joined);
        return model.generate(prompt);
    }
}
