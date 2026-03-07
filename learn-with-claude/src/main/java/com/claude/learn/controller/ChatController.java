package com.claude.learn.controller;

import com.claude.learn.agent.PolicyAgent;
import com.claude.learn.config.UserContext;
import com.claude.learn.service.PromptService;
import com.claude.learn.service.TokenMonitorService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api")
public class ChatController {


    private final PolicyAgent policyAgent;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final TokenMonitorService tokenMonitorService;
    private final PromptService promptService;

    private static final String DEFAULT_PROMPT = """
            你是一个企业内部智能助手，你有以下工具可以使用：
            1. searchPolicy：查询公司内部政策文档
            2. getWeather：查询城市天气
            请根据用户问题自主决定调用哪些工具，综合结果给出完整回答。
            """;


    public ChatController(PolicyAgent policyAgent,TokenMonitorService tokenMonitorService, PromptService promptService) {
        this.promptService = promptService;
        this.tokenMonitorService = tokenMonitorService;
        this.policyAgent = policyAgent;
    }

    @GetMapping("/usage")
    public ResponseEntity<?> usage() {
        String username = getCurrentUsername();
        return ResponseEntity.ok(Map.of(
                "username", username,
                "summary", tokenMonitorService.getUsageSummary(username)
        ));
    }

    @PostMapping("/chat")
    public ResponseEntity chat(@RequestBody ChatRequest request) {

        String username = getCurrentUsername();

        // 配额检查
        if (tokenMonitorService.isExceeded(username)) {
            return ResponseEntity.status(429)
                    .body(Map.of("error", "今日 Token 配额已用完，请明天再试"));
        }
        return ResponseEntity.ok(policyAgent.chat(request.message(),promptService.getPrompt("policy_agent", DEFAULT_PROMPT)));
    }

    public record ChatRequest(String message) {}

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String message){

        String username = getCurrentUsername();
        // 配额检查
        if (tokenMonitorService.isExceeded(username)) {
            SseEmitter emitter = new SseEmitter();
            try {
                emitter.send("今日 Token 配额已用完，请明天再试");
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 捕获当前 SecurityContext
        SecurityContext context = SecurityContextHolder.getContext();


        SseEmitter emitter = new SseEmitter(60_000L);
        //
        executor.submit(()->{
            // 手动设置到虚拟线程里
            SecurityContextHolder.setContext(context);
            try{
                policyAgent.streamChat(message,promptService.getPrompt("policy_agent", DEFAULT_PROMPT))
                        .onNext(token -> {
                            try{
                                emitter.send(token);
                            }catch (IOException e){
                                emitter.completeWithError(e);
                            }
                        })
                        .onComplete( response -> emitter.complete())
                .onError( error -> emitter.completeWithError(error))
                        .start();
            }catch (Exception e){
                emitter.completeWithError(e);
            } finally {
                SecurityContextHolder.clearContext();  // 用完清除，防止内存泄漏
            }
        });
        return  emitter;
    }

}
