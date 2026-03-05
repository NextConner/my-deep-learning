package com.example.aiagent.service.agent;

import com.example.aiagent.model.dto.llm.ChatCompletionRequest;
import com.example.aiagent.model.dto.llm.ChatCompletionResponse;
import com.example.aiagent.service.llm.LlmGatewayService;
import com.example.aiagent.service.tool.Tool;
import com.example.aiagent.service.tool.ToolRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Agent 服务
 * 支持工具调用（Function Calling）的智能 Agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final LlmGatewayService llmGatewayService;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    /** 最大工具调用次数，防止无限循环 */
    private static final int MAX_TOOL_CALLS = 5;

    /**
     * 运行 Agent（支持工具调用）
     * @param tenantId 租户ID
     * @param userMessage 用户消息
     * @return Agent 响应（包含答案和工具调用次数）
     */
    public AgentResponse run(String tenantId, String userMessage) {
        log.info("运行 Agent: tenant={}, message={}", tenantId, userMessage);
        
        // 1. 初始化消息列表
        List<ChatCompletionRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatCompletionRequest.Message("user", userMessage, null, null));
        
        // 2. 获取可用工具列表
        List<ChatCompletionRequest.Tool> tools = buildToolDefinitions();
        
        int toolCallCount = 0;
        String finalAnswer = null;
        
        // 3. Agent 循环：直到获得最终答案或达到最大调用次数
        while (toolCallCount < MAX_TOOL_CALLS) {
            // 3.1 调用 LLM（带工具）
            ChatCompletionResponse response = llmGatewayService.chatWithTools(messages, tools);
            
            // 3.2 将助手消息添加到对话历史
            ChatCompletionResponse.Message assistantMsg = response.choices().get(0).message();
            messages.add(new ChatCompletionRequest.Message(
                    assistantMsg.role(),
                    assistantMsg.content(),
                    null,  // 工具调用不需要保存在历史中
                    null
            ));
            
            // 3.3 检查是否有工具调用
            if (assistantMsg.toolCalls() == null || assistantMsg.toolCalls().isEmpty()) {
                // 没有更多工具调用，这是最终答案
                finalAnswer = assistantMsg.content();
                break;
            }
            
            // 3.4 执行工具调用
            for (ChatCompletionResponse.ToolCall toolCall : assistantMsg.toolCalls()) {
                String toolName = toolCall.function().name();
                String argsJson = toolCall.function().arguments();
                
                log.info("执行工具: {} 参数: {}", toolName, argsJson);
                
                try {
                    // 解析工具参数
                    Map<String, Object> arguments = objectMapper.readValue(argsJson, Map.class);
                    
                    // 为需要租户ID的工具注入 tenant_id
                    if (toolName.equals("search_documents")) {
                        arguments.put("tenant_id", tenantId);
                    }
                    
                    // 执行工具
                    Tool.ToolResult result = executeTool(toolName, arguments);
                    
                    // 将工具结果添加到对话历史
                    String resultContent = result.success() 
                            ? result.output() 
                            : "错误: " + result.error();
                    
                    messages.add(new ChatCompletionRequest.Message(
                            "tool",
                            resultContent,
                            null,
                            toolCall.id()
                    ));
                    
                } catch (JsonProcessingException e) {
                    log.error("解析工具参数失败: {}", e.getMessage());
                }
            }
            
            toolCallCount++;
        }
        
        // 4. 如果没有获得答案，返回超时消息
        if (finalAnswer == null) {
            finalAnswer = "已达到最大工具调用次数，无法完成请求。";
        }
        
        return new AgentResponse(finalAnswer, toolCallCount);
    }

    /**
     * 构建工具定义列表
     * 将所有注册的工具转换为 LLM 可理解的格式
     */
    private List<ChatCompletionRequest.Tool> buildToolDefinitions() {
        List<ChatCompletionRequest.Tool> tools = new ArrayList<>();
        
        for (Tool tool : toolRegistry.getAllTools().values()) {
            ChatCompletionRequest.Tool t = new ChatCompletionRequest.Tool(
                    "function",
                    new ChatCompletionRequest.Function(
                            tool.getName(),
                            tool.getDescription(),
                            tool.getParameters()
                    )
            );
            tools.add(t);
        }
        
        return tools;
    }

    /**
     * 执行工具
     */
    private Tool.ToolResult executeTool(String toolName, Map<String, Object> arguments) {
        return toolRegistry.getTool(toolName)
                .map(tool -> tool.execute(arguments))
                .orElse(Tool.ToolResult.error("未找到工具: " + toolName));
    }

    /**
     * Agent 响应
     * @param answer 最终答案
     * @param toolCallsUsed 使用的工具调用次数
     */
    public record AgentResponse(String answer, int toolCallsUsed) {}
}
