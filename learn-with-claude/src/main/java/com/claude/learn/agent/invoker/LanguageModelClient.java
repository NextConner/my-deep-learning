package com.claude.learn.agent.invoker;

//抽象
public interface LanguageModelClient {
    String chat(String message, String systemPrompt);
    String modelName();  // 用于日志和监控
}
