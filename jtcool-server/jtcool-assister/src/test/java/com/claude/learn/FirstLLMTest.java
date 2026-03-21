package com.claude.learn;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// 最简单的调用，先跑通这个
@SpringBootTest
class FirstLLMTest {

    @Autowired
    private ChatLanguageModel model;

    @Test
    void hello() {
        String response = model.generate("用一句话解释什么是RAG");
        System.out.println(response);
    }
}
