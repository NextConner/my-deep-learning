package com.claude.learn;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class RagQueryTest {


    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Test
    public  void query(){
        //输入
        String question = "出差酒店住两天能报销多少钱？";

        //1.问题向量化
        Embedding questEmbedding = embeddingModel.embed(question).content();

        //2. 向量匹配
        EmbeddingSearchRequest searchResult = EmbeddingSearchRequest.builder()
                .queryEmbedding(questEmbedding)
                .maxResults(3)
                .minScore(0.7d)
                .build();

        EmbeddingSearchResult<TextSegment> matchList = embeddingStore.search(searchResult);

        //3.show
        for (EmbeddingMatch<TextSegment> match : matchList.matches()) {
            System.out.println("相似度："  + match.score());
            System.out.println("内容:" + match.embedded().text());
            System.out.println("-----------------------------");
        }
    }

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Test
    void ragAnswer(){

        //输入
        String question = "出差酒店住两天能报销多少钱？";

        //1.问题向量化
        Embedding questEmbedding = embeddingModel.embed(question).content();

        //2. 向量匹配
        EmbeddingSearchRequest searchResult = EmbeddingSearchRequest.builder()
                .queryEmbedding(questEmbedding)
                .maxResults(3)
                .minScore(0.7d)
                .build();

        EmbeddingSearchResult<TextSegment> matchList = embeddingStore.search(searchResult);

        //把匹配到的内容拼接成上下文
        StringBuilder context = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : matchList.matches()) {
            context.append(match.embedded().text()).append("\n");
        }

        //3.把上下文和问题一起发送给语言模型，生成答案
        String prompt = """
            你是一个企业内部助手，只根据以下资料回答问题，不要编造信息。
            
            【参考资料】
            %s
            
            【用户问题】
            %s
            """.formatted(context, question);

        //丢给大模型组织答案
        String answer = chatLanguageModel.generate(prompt);

        System.out.println("问题: " + question);
        System.out.println("回答：" + answer);

    }

}
