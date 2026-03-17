package com.claude.learn.agent.invoker;

import com.claude.learn.agent.PolicyAgent;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public class ModelInvoker {

    private final PolicyAgent policyAgent;
    private final ChatLanguageModel ollamaAgent;
    private final CircuitBreaker circuitBreaker;

    public ModelInvoker(PolicyAgent policyAgent,
                        ChatLanguageModel ollamaAgent,
                        CircuitBreakerRegistry registry) {
        this.policyAgent = policyAgent;
        this.ollamaAgent = ollamaAgent;
        this.circuitBreaker = registry.circuitBreaker("deepseek");
    }

    public String invoke(String message, String systemPrompt) {
        return CircuitBreaker
                .decorateSupplier(circuitBreaker,
                        () -> policyAgent.chat(message, systemPrompt))
                .get();
    }

    // 降级方法：熔断触发时调用
    public String invokeWithFallback(String message, String systemPrompt) {
        try {
            return invoke(message, systemPrompt);
        } catch (CallNotPermittedException e) {
            // 熔断器 OPEN，直接切 Ollama
            return ollamaAgent.generate(message);
        } catch (Exception e) {
            // 其他异常，也降级到 Ollama
            return ollamaAgent.generate(message);
        }
    }
}
