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

/**
 * 工具执行包装器 - 统一工具调用的执行方式
 *
 * 设计目标：
 * 1. 统一HumanInLoopAgent和PolicyAgent的工具执行方式
 * 2. 提供超时控制和重试机制
 * 3. 增强可观测性（日志、执行时间）
 * 4. 保持与LangChain4j @Tool注解的兼容性
 *
 * 技术实现：
 * - 使用反射动态调用@Tool注解的方法
 * - 使用ExecutorService + Future实现超时控制
 * - 返回结构化的ToolExecutionResult
 *
 * 面试要点：
 * - 为什么用反射？保持与LangChain4j框架的兼容性，避免硬编码工具名称
 * - 如何实现超时？Future.get(timeout)，超时后cancel任务
 * - 重试策略？指数退避？当前是简单重试，可扩展为指数退避
 */
@Component
public class ToolExecutionWrapper {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionWrapper.class);

    private final AgentTools agentTools;
    private final AgentRuntimeProperties runtimeProperties;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public ToolExecutionWrapper(AgentTools agentTools, AgentRuntimeProperties runtimeProperties) {
        this.agentTools = agentTools;
        this.runtimeProperties = runtimeProperties;
    }

    /**
     * 执行工具调用，带超时和重试机制
     *
     * @param toolName 工具名称（对应@Tool注解的方法名）
     * @param arguments 工具参数（JSON格式字符串）
     * @return ToolExecutionResult 包含执行结果、耗时、错误信息
     */
    public ToolExecutionResult execute(String toolName, String arguments) {
        long startTime = System.currentTimeMillis();

        // 重试循环
        for (int attempt = 0; attempt <= runtimeProperties.getRetryTimes(); attempt++) {
            try {
                String result = executeWithTimeout(toolName, arguments);
                long executionTime = System.currentTimeMillis() - startTime;
                log.info("Tool {} executed successfully in {}ms", toolName, executionTime);
                return ToolExecutionResult.success(result, executionTime);
            } catch (TimeoutException e) {
                log.warn("Tool {} timed out on attempt {}", toolName, attempt + 1);
                if (attempt == runtimeProperties.getRetryTimes()) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    return ToolExecutionResult.failure(
                        "Tool execution timed out after " + runtimeProperties.getStepTimeoutMs() + "ms",
                        executionTime
                    );
                }
            } catch (Exception e) {
                log.error("Tool {} failed on attempt {}: {}", toolName, attempt + 1, e.getMessage());
                if (attempt == runtimeProperties.getRetryTimes()) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    return ToolExecutionResult.failure(
                        "Tool execution failed: " + e.getMessage(),
                        executionTime
                    );
                }
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        return ToolExecutionResult.failure("Unknown error", executionTime);
    }

    /**
     * 带超时控制的工具执行
     */
    private String executeWithTimeout(String toolName, String arguments) throws Exception {
        Future<String> future = executor.submit(() -> executeTool(toolName, arguments));
        try {
            return future.get(runtimeProperties.getStepTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        }
    }

    /**
     * 使用反射执行工具
     *
     * 实现原理：
     * 1. 遍历AgentTools的所有方法
     * 2. 找到带@Tool注解且方法名匹配的方法
     * 3. 解析参数并调用方法
     *
     * 面试要点：
     * - 为什么不用switch-case？扩展性差，每增加一个工具都要修改代码
     * - 反射的性能问题？对于LLM调用场景，反射开销可忽略（LLM调用耗时远大于反射）
     * - 如何优化？可以缓存Method对象，避免每次都遍历
     */
    private String executeTool(String toolName, String arguments) throws Exception {
        Method[] methods = agentTools.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Tool.class)) {
                if (method.getName().equals(toolName)) {
                    String arg = parseArgument(arguments);
                    return (String) method.invoke(agentTools, arg);
                }
            }
        }
        throw new IllegalArgumentException("Unknown tool: " + toolName);
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
