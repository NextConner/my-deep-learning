package com.claude.learn.agent.invoker;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class LangChain4jModelClient implements LanguageModelClient {

    private final ChatLanguageModel model;
    private final String name;

    @Override
    public String chat(String message, String systemPrompt) {
        List<ChatMessage> messages = List.of(
                SystemMessage.systemMessage(systemPrompt),
                UserMessage.userMessage(message)
        );
        return model.generate(messages).content().text();
    }

    @Override
    public String modelName() { return name; }
}
