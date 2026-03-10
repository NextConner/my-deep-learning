package com.claude.learn.agent.tool;

/**
 * 工具级别的重试策略配置
 *
 * 不同工具有不同的特性，需要不同的重试策略：
 * - searchPolicy（查询类）：可重试，失败通常是查不到数据
 * - sendEmail（操作类）：不能重试，可能重复发送
 * - getWeather（外部API）：可重试，API可能暂时故障
 *
 * 面试要点：
 * - 为什么要区分？提高成功率同时避免副作用
 * - 如何扩展？添加新工具时创建对应的策略
 * - 是否动态加载？可以从配置文件或数据库加载
 */
public class ToolRetryStrategy {
    private final String toolName;
    private final int maxRetryTimes;
    private final long timeoutMs;
    private final boolean allowDuplicateExecution;  // 是否允许重复执行（有副作用的操作应设为false）

    private ToolRetryStrategy(String toolName, int maxRetryTimes, long timeoutMs, boolean allowDuplicateExecution) {
        this.toolName = toolName;
        this.maxRetryTimes = maxRetryTimes;
        this.timeoutMs = timeoutMs;
        this.allowDuplicateExecution = allowDuplicateExecution;
    }

    /**
     * 获取工具的重试策略
     *
     * V2优化：根据工具类型调整参数
     */
    public static ToolRetryStrategy forTool(String toolName) {
        return switch (toolName) {
            // 查询类工具：可以重试，无副作用
            case "searchPolicy" -> new ToolRetryStrategy("searchPolicy", 2, 15000L, true);
            case "getWeather" -> new ToolRetryStrategy("getWeather", 2, 10000L, true);

            // 操作类工具：不能重试，有副作用
            case "sendEmail" -> new ToolRetryStrategy("sendEmail", 0, 10000L, false);
            case "updateUserProfile" -> new ToolRetryStrategy("updateUserProfile", 0, 10000L, false);

            // 默认策略：保守，允许1次重试
            default -> new ToolRetryStrategy(toolName, 1, 15000L, true);
        };
    }

    // Getters
    public String getToolName() {
        return toolName;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public boolean isAllowDuplicateExecution() {
        return allowDuplicateExecution;
    }

    @Override
    public String toString() {
        return "ToolRetryStrategy{" +
                "toolName='" + toolName + '\'' +
                ", maxRetryTimes=" + maxRetryTimes +
                ", timeoutMs=" + timeoutMs +
                ", allowDuplicateExecution=" + allowDuplicateExecution +
                '}';
    }
}
