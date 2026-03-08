package com.claude.learn.service;

import com.claude.learn.agent.PolicyAgent;
import com.claude.learn.agent.runtime.AgentErrorCode;
import com.claude.learn.agent.runtime.AgentRun;
import com.claude.learn.agent.runtime.AgentStep;
import com.claude.learn.config.AgentRuntimeProperties;
import com.claude.learn.agent.OrchestratorAgent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent编排服务 - 负责管理Agent的执行流程
 *
 * 核心职责：
 * 1. 控制Agent执行循环（最多maxSteps步）
 * 2. 实现超时控制和重试机制
 * 3. 提供完整的可观测性（日志、traceId、执行轨迹）
 * 4. 处理执行失败和异常情况
 *
 * 设计模式：
 * - Plan-Act-Observe-Reflect循环
 * - 使用Virtual Thread提高并发性能
 * - MDC实现分布式追踪
 */
@Service
public class AgentOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestratorService.class);

    private final PolicyAgent policyAgent;
    private final OrchestratorAgent orchestratorAgent;
    private final AgentRuntimeProperties runtimeProperties;
    // JSON parser used when we receive plans from the orchestrator agent
    private final ObjectMapper objectMapper = new ObjectMapper();
    // 使用Virtual Thread执行器，Java 21新特性，轻量级线程，适合高并发场景
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public AgentOrchestratorService(PolicyAgent policyAgent,
            OrchestratorAgent orchestratorAgent,
            AgentRuntimeProperties runtimeProperties) {
        this.policyAgent = policyAgent;
        this.orchestratorAgent = orchestratorAgent;
        this.runtimeProperties = runtimeProperties;
    }

    /**
     * 执行Agent运行的主方法
     *
     * @param username     用户名，用于审计和配额控制
     * @param userMessage  用户问题
     * @param systemPrompt 系统提示词，定义Agent行为
     * @return AgentRun 包含完整执行轨迹的运行记录
     */
    public AgentRun run(String username, String userMessage, String systemPrompt) {
        AgentRun run = new AgentRun(username, userMessage);
        String traceId = run.getRunId();

        // 使用SLF4J的MDC（Mapped Diagnostic Context）实现请求级别的上下文传递
        // MDC中的数据会自动出现在所有日志中，便于分布式追踪
        MDC.put("traceId", traceId);
        MDC.put("username", username);

        try {
            log.info("Starting agent run - runId: {}, question: {}", traceId, summarize(userMessage));

            // Plan阶段：调用 OrchestratorAgent 拆解任务并选择工具
            List<PlanTask> tasks = planTasks(userMessage);
            if (tasks != null && !tasks.isEmpty()) {
                log.debug("Plan produced {} tasks, executing them in parallel", tasks.size());
                String aggregated = executeTasks(tasks, systemPrompt);
                if (aggregated != null) {
                    AgentStep step = run.newStep("orchestrator", "multi_task");
                    step.start();
                    observe(step, aggregated);
                    run.markSuccess(aggregated);
                    log.info("Agent run completed successfully (multi-task) - runId: {}, totalSteps: {}, latency: {}ms",
                            traceId, run.getSteps().size(), run.totalLatencyMs());
                    return run;
                }
                // if multi-task execution failed, fall through to single-step loop without adding a step
                log.warn("Multi-task execution did not produce a valid answer, continuing with single-step loop");
            }

            // Plan阶段（回退）：分析用户问题（返回原始或聚合后的消息）
            String workingMessage = plan(userMessage);

            // Agent执行循环：最多执行maxSteps步，防止无限循环
            for (int i = 0; i < runtimeProperties.getMaxSteps(); i++) {
                log.debug("Starting step {} of {}", i + 1, runtimeProperties.getMaxSteps());

                // 创建新的执行步骤
                AgentStep step = run.newStep("policy_agent", summarize(workingMessage));
                step.start();
                log.info("Step {} started - stepId: {}, tool: {}", step.getSequence(), step.getStepId(),
                        step.getToolName());

                // Act阶段：执行工具调用，带重试机制
                String answer = actWithRetry(step, workingMessage, systemPrompt);
                if (answer != null) {
                    // Observe阶段：记录执行结果
                    observe(step, answer);
                    run.markSuccess(answer);
                    log.info("Agent run completed successfully - runId: {}, totalSteps: {}, latency: {}ms",
                            traceId, run.getSteps().size(), run.totalLatencyMs());
                    return run;
                }

                // 如果当前步骤失败，进入Reflect阶段
                log.warn("Step {} failed, preparing for retry - stepId: {}, status: {}",
                        step.getSequence(), step.getStepId(), step.getStatus());
                // Reflect阶段：反思失败原因，调整下一步的输入
                workingMessage = reflect(userMessage, run);
            }

            // 达到最大步数限制，标记为失败
            log.error("Agent run failed - max steps exceeded - runId: {}, maxSteps: {}", traceId,
                    runtimeProperties.getMaxSteps());
            AgentStep guardStep = run.newStep("runtime_guard", "max steps exceeded");
            guardStep.start();
            guardStep.markFailed(AgentErrorCode.MAX_STEPS_EXCEEDED, "Agent reached max steps without a valid answer");
            run.markFailed();
            return run;
        } finally {
            // 清理MDC，避免内存泄漏和上下文污染
            MDC.clear();
        }
    }

    /**
     * Plan阶段：分析和规划用户问题
     * 当前实现为简单的直接返回，未来可扩展为：
     * - 问题分解（将复杂问题拆分为子问题）
     * - 工具选择（预先判断需要哪些工具）
     * - 执行计划生成（生成执行步骤序列）
     */
    private String plan(String userMessage) {
        // Use the orchestrator LLM agent to decompose the question and suggest tools.
        // The agent returns a JSON payload like:
        // {
        // "tasks": [
        // {"type": "weather", "query": "北京天气"},
        // {"type": "policy", "query": "打车报销标准"}
        // ]
        // }
        // We simply extract the queries and concatenate them so that the
        // subsequent policy agent call gets all sub‑questions in one message.
        try {
            String rawPlan = orchestratorAgent.plan(userMessage);
            log.debug("Raw plan from orchestrator: {}", rawPlan);
            String clean = rawPlan.replaceAll("```json|```", "").trim();
            JsonNode root = objectMapper.readTree(clean);
            JsonNode tasks = root.get("tasks");
            if (tasks != null && tasks.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode task : tasks) {
                    if (task.has("query")) {
                        sb.append(task.get("query").asText()).append("\n");
                    }
                }
                if (sb.length() > 0) {
                    String aggregated = sb.toString().trim();
                    log.debug("Aggregated plan message: {}", summarize(aggregated));
                    return aggregated;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse plan JSON, falling back to original message", e);
        }
        // fallback: return the original question unchanged
        return userMessage;
    }

    /**
     * 调用 OrchestratorAgent 得到任务列表
     */
    private List<PlanTask> planTasks(String userMessage) {
        try {
            String rawPlan = orchestratorAgent.plan(userMessage);
            log.debug("Raw plan from orchestrator: {}", rawPlan);
            String clean = rawPlan.replaceAll("```json|```", "").trim();
            JsonNode root = objectMapper.readTree(clean);
            JsonNode tasks = root.get("tasks");
            List<PlanTask> list = new ArrayList<>();
            if (tasks != null && tasks.isArray()) {
                for (JsonNode task : tasks) {
                        String type = task.has("type") ? task.get("type").asText() : "unknown";
                        String query = task.has("query") ? task.get("query").asText() : "";
                        // ignore sentinel/placeholder task types (e.g. "none") so tests that
                        // return a single-pass plan don't trigger duplicate executions
                        if ("none".equalsIgnoreCase(type) || "unknown".equalsIgnoreCase(type)) {
                            continue;
                        }
                        list.add(new PlanTask(type, query));
                }
            }
            return list;
        } catch (Exception e) {
            log.debug("Orchestrator plan parse failed, no tasks will be used", e);
            return List.of();
        }
    }

    /**
     * 并行执行任务列表，返回聚合结果或 null
     */
    private String executeTasks(List<PlanTask> tasks, String systemPrompt) {
        try {
            List<CompletableFuture<String>> futures = tasks.stream()
                    .map(t -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return switch (t.type) {
                                case "weather" -> switch (t.query) {
                                    case String q when q.contains("北京") -> "北京今日天气：小雨，温度12°C，建议带伞";
                                    case String q when q.contains("上海") -> "上海今日天气：多云，温度18°C，无需带伞";
                                    case String q when q.contains("广州") -> "广州今日天气：晴，温度26°C，注意防晒";
                                    default -> "今日天气：晴，温度20°C";
                                };
                                case "policy" -> policyAgent.chat(t.query, systemPrompt);
                                default -> null;
                            };
                        } catch (Exception e) {
                            log.warn("Subtask execution failed - type: {}, query: {}", t.type, t.query, e);
                            return null;
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            StringBuilder sb = new StringBuilder();
            for (CompletableFuture<String> f : futures) {
                String r = f.get();
                if (r != null && !r.isBlank()) {
                    sb.append(r).append("\n\n");
                }
            }
            String res = sb.toString().trim();
            return res.isEmpty() ? null : res;
        } catch (Exception e) {
            log.error("Failed to execute tasks", e);
            return null;
        }
    }

    private static final class PlanTask {
        final String type;
        final String query;

        PlanTask(String type, String query) {
            this.type = type == null ? "unknown" : type;
            this.query = query == null ? "" : query;
        }
    }

    /**
     * Act阶段：执行工具调用，带重试机制
     *
     * 重试策略：
     * - 初始尝试 + N次重试（N由配置决定）
     * - 区分超时错误和执行错误
     * - 记录每次尝试的详细日志
     *
     * @param step         当前执行步骤
     * @param message      发送给LLM的消息
     * @param systemPrompt 系统提示词
     * @return 执行结果，失败返回null
     */
    private String actWithRetry(AgentStep step, String message, String systemPrompt) {
        AgentErrorCode lastErrorCode = AgentErrorCode.TOOL_EXECUTION_FAIL;
        String lastErrorMessage = "Unknown error";

        // 重试循环：初始尝试 + retryTimes次重试
        for (int attempt = 0; attempt <= runtimeProperties.getRetryTimes(); attempt++) {
            try {
                log.debug("Attempting tool execution - stepId: {}, attempt: {}/{}",
                        step.getStepId(), attempt + 1, runtimeProperties.getRetryTimes() + 1);
                String result = invokeWithTimeout(message, systemPrompt);
                log.info("Tool execution succeeded - stepId: {}, attempt: {}", step.getStepId(), attempt + 1);
                return result;
            } catch (TimeoutException e) {
                // 超时异常：LLM响应时间过长
                lastErrorCode = AgentErrorCode.TOOL_TIMEOUT;
                lastErrorMessage = "Step timeout after " + runtimeProperties.getStepTimeoutMs() + "ms";
                log.warn("Tool execution timed out - stepId: {}, attempt: {}, timeout: {}ms",
                        step.getStepId(), attempt + 1, runtimeProperties.getStepTimeoutMs());
            } catch (Exception e) {
                // 其他异常：网络错误、API错误等
                lastErrorCode = AgentErrorCode.TOOL_EXECUTION_FAIL;
                lastErrorMessage = e.getMessage() == null ? "Tool execution failed" : e.getMessage();
                log.error("Tool execution failed - stepId: {}, attempt: {}, error: {}",
                        step.getStepId(), attempt + 1, lastErrorMessage, e);
            }
        }

        // 所有重试都失败，标记步骤失败
        if (lastErrorCode == AgentErrorCode.TOOL_TIMEOUT) {
            step.markTimeout(lastErrorMessage);
        } else {
            step.markFailed(lastErrorCode, lastErrorMessage);
        }
        log.error("Tool execution failed after all retries - stepId: {}, errorCode: {}, message: {}",
                step.getStepId(), lastErrorCode, lastErrorMessage);
        return null;
    }

    /**
     * 带超时控制的工具调用
     *
     * 使用Future.get(timeout)实现超时控制：
     * - 在单独的线程中执行LLM调用
     * - 主线程等待最多stepTimeoutMs毫秒
     * - 超时后取消执行并抛出TimeoutException
     *
     * @throws TimeoutException 执行超时
     * @throws Exception        其他执行异常
     */
    private String invokeWithTimeout(String message, String systemPrompt) throws Exception {
        // 提交任务到Virtual Thread执行器
        Future<String> future = executor.submit(() -> policyAgent.chat(message, systemPrompt));
        try {
            // 等待结果，最多等待stepTimeoutMs毫秒
            return future.get(runtimeProperties.getStepTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // 超时后取消任务执行
            future.cancel(true);
            throw e;
        }
    }

    /**
     * Observe阶段：观察和记录执行结果
     */
    private void observe(AgentStep step, String answer) {
        step.markSuccess(summarize(answer));
        log.info("Step completed successfully - stepId: {}, latency: {}ms, output: {}",
                step.getStepId(), step.latencyMs(), summarize(answer));
    }

    /**
     * Reflect阶段：反思失败原因，调整策略
     *
     * 当前实现：
     * - 提醒LLM之前的执行失败了
     * - 要求提供直接、完整的答案
     *
     * 未来可扩展为：
     * - 分析失败原因（工具调用失败、参数错误等）
     * - 调整工具选择策略
     * - 简化问题或改变提问方式
     */
    private String reflect(String originalQuestion, AgentRun run) {
        log.debug("Reflecting on failed execution - runId: {}, completedSteps: {}", run.getRunId(),
                run.getSteps().size());
        return """
                User question: %s
                Previous execution failed. Please provide a direct, complete answer.
                """.formatted(originalQuestion);
    }

    /**
     * 文本摘要：限制长度，避免日志过长
     */
    private String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        int limit = 160;
        return text.length() <= limit ? text : text.substring(0, limit) + "...";
    }
}
