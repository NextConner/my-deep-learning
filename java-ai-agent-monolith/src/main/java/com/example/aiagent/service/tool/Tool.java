package com.example.aiagent.service.tool;

import java.util.Map;

/**
 * 工具接口
 * 所有 Agent 工具必须实现此接口
 */
public interface Tool {
    
    /**
     * 获取工具名称
     * @return 工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     * @return 工具描述（供 LLM 理解工具用途）
     */
    String getDescription();
    
    /**
     * 获取工具参数定义
     * @return JSON Schema 格式的参数定义
     */
    Map<String, Object> getParameters();
    
    /**
     * 执行工具
     * @param arguments 工具参数
     * @return 工具执行结果
     */
    ToolResult execute(Map<String, Object> arguments);
    
    /**
     * 工具执行结果
     * @param success 是否成功
     * @param output 成功时的输出
     * @param error 失败时的错误信息
     */
    record ToolResult(boolean success, String output, String error) {
        /**
         * 创建成功结果
         */
        public static ToolResult ok(String output) {
            return new ToolResult(true, output, null);
        }
        
        /**
         * 创建错误结果
         */
        public static ToolResult error(String error) {
            return new ToolResult(false, null, error);
        }
    }
}
