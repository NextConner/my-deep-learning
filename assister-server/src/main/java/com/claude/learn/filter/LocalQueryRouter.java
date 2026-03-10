package com.claude.learn.filter;

import dev.ai4j.openai4j.chat.SystemMessage;
import dev.ai4j.openai4j.chat.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本地模型前置路由
 */
@Component
public class LocalQueryRouter {

    private final ChatLanguageModel ollamaModel;

    private static final String SYSTEM_PROMPT = """
            You are a query classifier. Reply with ONLY "yes" or "no". No other text.
            
            Reply "no" if the question is:
            - General knowledge (science, history, math, programming concepts)
            - Small talk or greetings
            - Asking you to write code or explain a technical concept
            
            Reply "yes" for everything else (company policies, internal processes,
            business rules, HR questions, expense rules, etc.)
            """;

    public LocalQueryRouter(ChatLanguageModel ollamaModel) {
        this.ollamaModel = ollamaModel;
    }

    public boolean needsRetrieval(String question) {
        List<ChatMessage> messages = List.of(
                dev.langchain4j.data.message.SystemMessage.systemMessage(SYSTEM_PROMPT),
                dev.langchain4j.data.message.UserMessage.userMessage("Question: " + question)
        );
        AiMessage response = ollamaModel.generate(messages).content();
        return response.text().trim().toLowerCase().startsWith("yes");
    }
}