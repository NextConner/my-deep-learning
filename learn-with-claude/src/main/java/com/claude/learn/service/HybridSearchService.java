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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


    /**
     *  混合检索：向量检索 + 关键词检索，结构融合返回
     */
    public List<String> hybridSearch(String query, int topK){

        //第一路：向量检索
        Embedding queryEmbedding = embeddingModel .embed(query).content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .minScore(0.5)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> vectorMatches = searchResult.matches();

        //第二路：关键词检索
        List<DocumentSegment> keywordMatches = repository.fullTextSearch(query, topK);

        //结果融合（RRF:倒数排名融合）
        Map<String,Double> scoreMap = new LinkedHashMap<>();

        //向量检索结果打分
        for (int i = 0; i < vectorMatches.size(); i++) {
            EmbeddingMatch<TextSegment> match = vectorMatches.get(i);
            String content = match.embedded().text();
            double rffScore = 1.0 / (60 + i + 1); // RRF打分，排名越靠前分数越高
            scoreMap.merge(content, rffScore, Double::sum);
        }

        // 关键词检索结果打分
        for (int i = 0; i < keywordMatches.size(); i++) {
            String content = keywordMatches.get(i).getContent();
            double rrfScore = 1.0 / (60 + i + 1);
            scoreMap.merge(content, rrfScore, Double::sum);
        }

        //按分数排序，取 topK
        return scoreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .toList();
    }


}
