package com.claude.learn.agent.tool;

import com.claude.learn.agent.AgentTools;
import com.claude.learn.config.AgentRuntimeProperties;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具执行包装器 - 统一工具调用的执行方式
 *
 * 设计目标：
 * 1. 统一HumanInLoopAgent和PolicyAgent的工具执行方式
 * 2. 提供超时控制和重试机制
 * 3. 增强可观测性（日志、执行时间）
 * 4. 保持与LangChain4j @Tool注解的兼容性
 * 5. V2优化：根据工具类型和错误类型调整策略
 * 6. V3优化：基于执行统计动态调整
 *
 * 技术实现：
 * - 使用反射动态调用@Tool注解的方法
 * - 使用ExecutorService + Future实现超时控制
 * - 返回结构化的ToolExecutionResult
 * - 区分错误类型，决定是否重试
 * - 为每个工具配置不同的重试策略
 * - 收集执行统计，用于动态调整
 *
 * 面试要点：
 * - 为什么用反射？保持与LangChain4j框架的兼容性，避免硬编码工具名称
 * - 如何实现超时？Future.get(timeout)，超时后cancel任务
 * - 重试策略？V2支持根据错误类型和工具类型调整
 * - 动态调整？V3基于SLA自动推荐参数修改
 */
@Component
public class ToolExecutionWrapper {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionWrapper.class);

    private final AgentTools agentTools;
    private final AgentRuntimeProperties runtimeProperties;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    
    // V2优化：缓存工具方法，避免每次都反射遍历
    private final java.util.Map<String, Method> methodCache = new ConcurrentHashMap<>();
    
    // V3优化：统计每个工具的执行情况，用于动态调整
    private final java.util.Map<String, ToolExecutionStats> statsMap = new ConcurrentHashMap<>();

    public ToolExecutionWrapper(AgentTools agentTools, AgentRuntimeProperties runtimeProperties) {
        this.agentTools = agentTools;
        this.runtimeProperties = runtimeProperties;
        // 初始化方法缓存
        initMethodCache();
    }
    
    /**
     * 初始化方法缓存，加快后续的工具查找
     */
    private void initMethodCache() {
        Method[] methods = agentTools.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Tool.class)) {
                methodCache.put(method.getName(), method);
            }
        }
        log.info("Initialized method cache with {} tools", methodCache.size());
    }
    
    /**
     * 获取工具的执行统计，如果没有则创建
     */
    private ToolExecutionStats getOrCreateStats(String toolName) {
        return statsMap.computeIfAbsent(toolName, k -> new ToolExecutionStats(toolName));
    }
    
    /**
     * 获取所有工具的执行统计（用于监控和告警）
     */
    public java.util.Map<String, ToolExecutionStats> getAllStats() {
        return new java.util.HashMap<>(statsMap);
    }

    /**
     * 执行工具调用，带超时和重试机制
     *
     * V2优化：
     * 1. 根据工具类型选择不同的重试策略
     * 2. 根据错误类型决定是否重试
     * 3. 对某些错误类型应用指数退避
     *
     * @param toolName 工具名称（对应@Tool注解的方法名）
     * @param arguments 工具参数（JSON格式字符串）
     * @return ToolExecutionResult 包含执行结果、耗时、错误信息
     */
    public ToolExecutionResult execute(String toolName, String arguments) {
        long startTime = System.currentTimeMillis();
        ToolExecutionStats stats = getOrCreateStats(toolName);
        
        // V2优化：获取工具特定的重试策略
        ToolRetryStrategy strategy = ToolRetryStrategy.forTool(toolName);
        int maxRetries = strategy.getMaxRetryTimes();
        long toolTimeout = strategy.getTimeoutMs();

        // 重试循环 - V2改进：支持工具级和错误级的retry决策
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            long attemptStartTime = System.currentTimeMillis();
            
            try {
                String result = executeWithTimeout(toolName, arguments, toolTimeout);
                long executionTime = System.currentTimeMillis() - startTime;
                
                // V3优化：记录成功统计
                stats.recordSuccess(executionTime);
                
                log.info("Tool {} executed successfully in {}ms (attempt {})", 
                    toolName, executionTime, attempt + 1);
                return ToolExecutionResult.success(result, executionTime);
                
            } catch (TimeoutException e) {
                long attemptTime = System.currentTimeMillis() - attemptStartTime;
                RetryableErrorType errorType = RetryableErrorType.classify(e);
                
                log.warn("Tool {} timed out on attempt {}, error type: {}", 
                    toolName, attempt + 1, errorType);
                
                // V2优化：根据错误类型决定是否继续重试
                if (!errorType.isRetryable() || attempt == maxRetries) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    stats.recordTimeout(executionTime);
                    return ToolExecutionResult.failure(
                        "Tool execution timed out after " + toolTimeout + "ms",
                        executionTime
                    );
                }
                
                // V2优化：对某些错误应用指数退避
                if (errorType == RetryableErrorType.RETRYABLE_WITH_BACKOFF && 
                    errorType.getBackoffDelayMs() > 0) {
                    try {
                        long backoffMs = errorType.getBackoffDelayMs() * (long) Math.pow(2, attempt);
                        log.info("Applying backoff of {}ms before retry", backoffMs);
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                
            } catch (Exception e) {
                long attemptTime = System.currentTimeMillis() - attemptStartTime;
                RetryableErrorType errorType = RetryableErrorType.classify(e);
                
                log.error("Tool {} failed on attempt {}, error type: {}, message: {}", 
                    toolName, attempt + 1, errorType, e.getMessage());
                
                // V2优化：根据错误类型决定是否继续重试
                if (!errorType.isRetryable() || attempt == maxRetries) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    stats.recordFailure(executionTime);
                    return ToolExecutionResult.failure(
                        "Tool execution failed: " + e.getMessage(),
                        executionTime
                    );
                }
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        stats.recordFailure(executionTime);
        return ToolExecutionResult.failure("Unknown error", executionTime);
    }

    /**
     * 带超时控制的工具执行（支持工具级别的超时）
     *
     * @param toolName 工具名称
     * @param arguments 工具参数
     * @param timeoutMs 超时时间（毫秒）
     */
    private String executeWithTimeout(String toolName, String arguments, long timeoutMs) throws Exception {
        Future<String> future = executor.submit(() -> executeTool(toolName, arguments));
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        }
    }

    /**
     * 使用反射执行工具（优化版本）
     *
     * V2优化：使用方法缓存，避免每次都遍历所有方法
     *
     * 实现原理：
     * 1. 从缓存中查找方法（如果初始化过）
     * 2. 找到带@Tool注解且方法名匹配的方法
     * 3. 解析参数并调用方法
     *
     * 面试要点：
     * - 为什么用反射？保持与LangChain4j框架的兼容性，避免硬编码工具名称
     * - 性能是否是问题？对于LLM调用场景，反射开销可忽略（LLM调用耗时远大于反射）
     * - 如何优化？缓存Method对象，避免每次都遍历（已实现）
     */
    private String executeTool(String toolName, String arguments) throws Exception {
        // V2优化：先从缓存查找
        Method method = methodCache.get(toolName);
        
        if (method == null) {
            // 缓存未命中，动态查找
            Method[] methods = agentTools.getClass().getMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(Tool.class) && m.getName().equals(toolName)) {
                    method = m;
                    methodCache.put(toolName, m);  // 缓存起来
                    break;
                }
            }
        }
        
        if (method == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
        
        String arg = parseArgument(arguments);
        return (String) method.invoke(agentTools, arg);
    }

    /**
     * 简单的参数解析
     * 从JSON格式字符串中提取值
     * 例如：{"city":"Beijing"} -> "Beijing"
     *
     * 面试要点：
     * - 为什么不用Jackson？当前场景简单，正则足够；复杂场景应该用JSON库
     * - 如何处理多参数？当前只支持单参数，多参数需要改进
     */
    private String parseArgument(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return "";
        }
        return arguments.replaceAll(".*\":(\\s*\"?)([^\"|}]+).*", "$2").trim();
    }
}
