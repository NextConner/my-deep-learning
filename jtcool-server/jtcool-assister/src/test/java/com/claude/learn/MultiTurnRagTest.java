package com.claude.learn;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MultiTurnRagTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private ChatMemory chatMemory;

    // 检索 + 回答 + 记忆
    private String chat(String userQuestion){

        //向量文本检索
        Embedding questionEmbedding = embeddingModel.embed(userQuestion)
                .content();

        //1. 向量匹配
        EmbeddingSearchRequest searchResult = EmbeddingSearchRequest.builder()
                .queryEmbedding(questionEmbedding)
                .maxResults(3)
                .minScore(0.7d)
                .build();

        EmbeddingSearchResult<TextSegment> matchList = embeddingStore.search(searchResult);
        //2. 拼接到检索上下文
        StringBuilder context = new StringBuilder();
        matchList.matches()
                .forEach( match -> {
                    context.append(match.embedded().text()).append("\n");
                } );

        //3.构造输入 prompt
        String promptWithContext = """
            你是一个企业内部助手，只根据以下资料回答问题，不要编造信息。
            
            【参考资料】
            %s
            
            【用户问题】
            %s
            """.formatted(context, userQuestion);
        //4.对话记忆
        chatMemory.add(UserMessage.from(promptWithContext));

        //5.输入给LLM
        List<ChatMessage> messageList = chatMemory.messages();
        //6.补全提示,利用历史对话内容
        messageList.add(0, SystemMessage.from("你是一个企业内部助手，只根据以下资料回答问题，不要编造信息。如果资料不足，结合对话历史推断。"));
        //
        Response<AiMessage> response = chatLanguageModel.generate(messageList);
        String answer = response.content().text();
        //回答也进行记忆
        chatMemory.add(AiMessage.from(answer));

        return  answer;

    }

    //测试多轮对话
    @Test
    void multiTurnChat(){

        // 第一轮
        String q1 = "飞机票怎么报销？";
        System.out.println("用户：" + q1);
        System.out.println("AI：" + chat(q1));
        System.out.println("---");

        // 第二轮：故意用模糊问法，依赖上下文
        String q2 = "那商务舱呢？";
        System.out.println("用户：" + q2);
        System.out.println("AI：" + chat(q2));
        System.out.println("---");

        // 第三轮：继续追问
        String q3 = "酒店呢，北京能报多少？";
        System.out.println("用户：" + q3);
        System.out.println("AI：" + chat(q3));

    }


}
