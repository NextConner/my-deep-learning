package com.claude.learn;


import com.claude.learn.service.HybridSearchService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class HybridSearchTest {


    @Autowired
    private HybridSearchService hybridSearchService;

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Test
    void testHybridSearch() {

        // 测试1：语义检索擅长的问题
        System.out.println("=== 测试1：语义问题 ===");
        ask("出差住酒店能报多少钱？");

        // 测试2：关键词检索擅长的问题
        System.out.println("=== 测试2：关键词问题 ===");
        ask("第3条是什么？");

        // 测试3：混合都需要的问题
        System.out.println("=== 测试3：混合问题 ===");
        ask("500元的限额是针对哪类城市的？");

    }

    //混合检索方法
    private void ask(String question){

        //混合检索
        List<String> results = hybridSearchService.hybridSearch(question,3);

        //拼接上下文
        String context = String.join("\n", results);

        //构造上下文
        List<ChatMessage> messages = List.of(
                SystemMessage.from("你是企业内部助手,只根据参考资料回答问题,如果没有相关资料,请说不知道"),
                UserMessage.from("""
                        【参考资料】
                        %s
                        
                        【问题】
                        %s
                        """.formatted(context, question))
        );

        //
        Response<AiMessage> response = chatLanguageModel.generate(messages);
        System.out.println("问题： " + question);
        System.out.println("回答：" + response.content().text());
        System.out.println(" ------  ");

    }

}
