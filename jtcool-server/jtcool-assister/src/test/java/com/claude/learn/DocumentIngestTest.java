package com.claude.learn;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
public class DocumentIngestTest {


    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Test
    void ingestPdf(){

        //加载PDF
        Path ingestPath = Paths.get("src/test/resources/docs/policy.pdf");
        Document document = FileSystemDocumentLoader.loadDocument(ingestPath,
                new ApachePdfBoxDocumentParser());
        System.out.println("文档长度:" + document.text().length());

        //分块
        var splitter = DocumentSplitters.recursive(300,30);
        List<TextSegment> segments = splitter.split(document);
        System.out.println("分块数量：" + segments.size());

        //embedding  + 入库
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings,segments);
        System.out.println("PDF 向量化入库完成！");

    }

    @Autowired
    private ChatMemory ChatMemory;

    @Test
    void testPdfEmbeddingAnswer(){
        //输入
        String question = "那商户舱呢？";



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
