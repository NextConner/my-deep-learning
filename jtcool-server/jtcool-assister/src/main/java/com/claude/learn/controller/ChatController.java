package com.claude.learn.controller;

import com.claude.learn.agent.PolicyAgent;
import com.claude.learn.agent.runtime.AgentRun;
import com.claude.learn.agent.runtime.AgentRunStatus;
import com.claude.learn.agent.runtime.AgentStep;
import com.claude.learn.config.AgentRuntimeProperties;
import com.claude.learn.service.AgentOrchestratorService;
import com.claude.learn.service.OutputSecurityService;
import com.claude.learn.service.PromptService;
import com.claude.learn.service.QuestionSuggestionService;
import com.claude.learn.service.TokenMonitorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.claude.learn.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private static final String DEFAULT_PROMPT = """
            你是一个企业内部知识助手，请根据提供的工具来帮助用户，
            1. searchPolicy-查询公司内部政策和文档
            2. getWeather-查询天气信息
            如果用户问你无法解答的问题或者没有对应的工具，请综合告诉用户无法解答相关问题。
            """;

    private final PolicyAgent policyAgent;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final TokenMonitorService tokenMonitorService;
    private final PromptService promptService;
    private final AgentOrchestratorService agentOrchestratorService;
    private final AgentRuntimeProperties runtimeProperties;
    private final OutputSecurityService outputSecurityService;
    private final ObjectMapper objectMapper;
    private final QuestionSuggestionService questionSuggestionService;

    public ChatController(PolicyAgent policyAgent,
                          TokenMonitorService tokenMonitorService,
                          PromptService promptService,
                          AgentOrchestratorService agentOrchestratorService,
                          AgentRuntimeProperties runtimeProperties,
                          OutputSecurityService outputSecurityService,
                          ObjectMapper objectMapper,
                          QuestionSuggestionService questionSuggestionService) {
        this.policyAgent = policyAgent;
        this.tokenMonitorService = tokenMonitorService;
        this.promptService = promptService;
        this.agentOrchestratorService = agentOrchestratorService;
        this.runtimeProperties = runtimeProperties;
        this.outputSecurityService = outputSecurityService;
        this.objectMapper = objectMapper;
        this.questionSuggestionService = questionSuggestionService;
    }

    @GetMapping("/usage")
    public ResponseEntity<?> usage() {
        String username = getCurrentUsername();
        return ResponseEntity.ok(Map.of(
                "username", username,
                "summary", tokenMonitorService.getUsageSummary(username)
        ));
    }

    @PostMapping("/suggestions")
    public ResponseEntity<?> getQuestionSuggestions(@RequestBody RouteContext routeContext) {
        List<String> suggestions = questionSuggestionService.getSuggestions(routeContext.module());
        return ResponseEntity.ok(Map.of("suggestions", suggestions));
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {

        String username = getCurrentUsername();
        log.info("Received chat request - username: {}, message: {}", username, request.message());

        if (tokenMonitorService.isExceeded(username)) {
            log.warn("Token quota exceeded - username: {}", username);
            return ResponseEntity.status(429)
                    .body(Map.of("error", "令牌配额已超出"));
        }

        String systemPrompt = promptService.getPrompt("policy_agent", DEFAULT_PROMPT);
        AgentRun run = agentOrchestratorService.run(username, request.message(), systemPrompt);

        if (run.getStatus() == AgentRunStatus.SUCCESS) {
            log.info("Chat request completed successfully - username: {}, runId: {}, latency: {}ms",
                    username, run.getRunId(), run.totalLatencyMs());

            String sanitizedAnswer = outputSecurityService.sanitize(run.getFinalAnswer());

            if (runtimeProperties.isIncludeTraceInResponse()) {
                ChatResponse raw = ChatResponse.from(run, true);
                ChatResponse sanitized = new ChatResponse(
                        sanitizedAnswer,
                        raw.runId(),
                        raw.status(),
                        raw.totalLatencyMs(),
                        raw.totalSteps(),
                        raw.startedAt(),
                        raw.endedAt(),
                        raw.steps()
                );
                return ResponseEntity.ok(sanitized);
            } else {
                return ResponseEntity.ok(Map.of("answer", sanitizedAnswer));
            }
        }

        log.error("Chat request failed - username: {}, runId: {}, status: {}, steps: {}",
                username, run.getRunId(), run.getStatus(), run.getSteps().size());

        if (runtimeProperties.isIncludeTraceInResponse()) {
            return ResponseEntity.status(500).body(ChatResponse.from(run, true));
        } else {
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", "执行失败，请稍后重试",
                            "runId", run.getRunId()
                    ));
        }
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String message) {

        String username = getCurrentUsername();
        log.info("Received streaming chat request - username: {}, message: {}", username, message);

        if (tokenMonitorService.isExceeded(username)) {
            log.warn("Token quota exceeded for streaming request - username: {}", username);
            SseEmitter emitter = new SseEmitter();
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(new AgentStreamEvent.ErrorEvent(null, "令牌配额已超出", "QUOTA_EXCEEDED")));
                emitter.complete();
            } catch (IOException e) {
                log.error("Error sending quota exceeded message", e);
                emitter.completeWithError(e);
            }
            return emitter;
        }

        SecurityContext context = SecurityContextHolder.getContext();
        SseEmitter emitter = new SseEmitter(60_000L);

        emitter.onTimeout(() -> {
            log.warn("SSE emitter timed out - username: {}", username);
            emitter.complete();
        });
        emitter.onError((ex) -> log.error("SSE emitter error - username: {}", username, ex));

        executor.submit(() -> {
            SecurityContextHolder.setContext(context);
            try {
                log.debug("Starting streaming response with orchestrator - username: {}", username);

                String systemPrompt = promptService.getPrompt("policy_agent", DEFAULT_PROMPT);
                AgentRun run = agentOrchestratorService.run(username, message, systemPrompt);

                // Emit step events
                for (AgentStep step : run.getSteps()) {
                    emitter.send(SseEmitter.event()
                            .name("step_start")
                            .data(new AgentStreamEvent.StepStartEvent(
                                    step.getStepId(),
                                    step.getSequence(),
                                    step.getToolName(),
                                    step.getInputSummary()
                            )));

                    emitter.send(SseEmitter.event()
                            .name("step_complete")
                            .data(new AgentStreamEvent.StepCompleteEvent(
                                    step.getStepId(),
                                    step.getSequence(),
                                    step.getStatus().name(),
                                    step.latencyMs(),
                                    step.getOutputSummary()
                            )));
                }

                // Check if run was successful
                if (run.getStatus() == AgentRunStatus.SUCCESS) {
                    // Stream the final answer token by token
                    String answer = outputSecurityService.sanitize(run.getFinalAnswer());
                    if (answer != null) {
                        for (char c : answer.toCharArray()) {
                            emitter.send(SseEmitter.event()
                                    .name("token")
                                    .data(String.valueOf(c)));
                        }
                    }

                    emitter.send(SseEmitter.event()
                            .name("complete")
                            .data(new AgentStreamEvent.CompleteEvent(
                                    run.getRunId(),
                                    answer,
                                    run.getSteps().size(),
                                    run.totalLatencyMs()
                            )));

                    log.info("Streaming response completed successfully - username: {}, runId: {}", username, run.getRunId());
                    emitter.complete();
                } else {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(new AgentStreamEvent.ErrorEvent(
                                    run.getRunId(),
                                    "执行失败，请稍后重试",
                                    run.getStatus().name()
                            )));
                    log.error("Streaming response failed - username: {}, runId: {}, status: {}", username, run.getRunId(), run.getStatus());
                    emitter.complete();
                }
            } catch (Exception e) {
                log.error("Unexpected error in streaming response - username: {}", username, e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(new AgentStreamEvent.ErrorEvent(null, e.getMessage(), "INTERNAL_ERROR")));
                } catch (IOException ex) {
                    log.error("Error sending error event", ex);
                }
                emitter.completeWithError(e);
            } finally {
                SecurityContextHolder.clearContext();
            }
        });
        return emitter;
    }

    public record ChatRequest(String message) {
    }

    public record RouteContext(String path, String name, String module) {
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "anonymous";
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.username();
        }
        return auth.getName();
    }
}
