package com.claude.learn.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface PolicyAgent {

    /**
     * 同步输出版本
     * @param userMessage
     * @return
     */
    String chat(@UserMessage String userMessage, @V("systemPrompt") String systemMessage);

    /**
     * 流式输出
     */
    TokenStream streamChat(@UserMessage String userMessage,  @V("systemPrompt")  String systemMessage);

}
