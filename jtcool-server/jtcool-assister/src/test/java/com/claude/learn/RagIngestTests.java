package com.claude.learn;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 文本向量化测试用例
 */
@SpringBootTest
public class RagIngestTests {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Test
    void ingestDocument(){

        // 模拟一份公司内部文档
        String content = """
                 公司差旅报销政策：
                1. 火车票：全额报销，需提供纸质票据或电子票截图。
                2. 飞机票：经济舱全额报销，商务舱需总监级以上审批。
                3. 酒店：一线城市每晚上限500元，二线城市每晚上限350元。
                4. 餐饮：每人每天上限150元，需提供发票。
                5. 打车：须使用公司指定平台，单次上限100元。
                """;
        //1.分块
        var document = Document.from(content);
        var splitter = DocumentSplitters.recursive(200,20);
        List<TextSegment> segments = splitter.split(document);
        System.out.println("分块结果：" + segments.size());

        //Embedding 向量化
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        System.out.println("向量数量：" + embeddings);

        //写入向量库
        embeddingStore.addAll(embeddings,segments);
        System.out.println("✅ 文档已存入向量库");
    }

}
