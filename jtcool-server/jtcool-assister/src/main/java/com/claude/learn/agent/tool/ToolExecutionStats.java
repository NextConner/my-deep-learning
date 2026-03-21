package com.claude.learn.agent.tool;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 工具执行统计 - 用于动态调整策略
 *
 * V3优化：基于实际执行数据，动态调整超时时间和重试次数
 *
 * 设计思想：
 * - 收集每个工具的执行统计（成功/失败/超时）
 * - 计算成功率、平均耗时、p95耗时
 * - 根据统计数据决策是否需要调整参数
 *
 * 面试要点：
 * - 如何定义SLA？比如成功率应该>95%
 * - 什么时候触发告警？失败率>30%
 * - 是否需要持久化？生产环境应该保存到数据库或指标系统
 * - 是否自动调整？可以用AI决策引擎，但通常需要人工审批
 */
public class ToolExecutionStats {
    private final String toolName;
    
    // 执行次数
    private final AtomicInteger totalExecutions = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger timeoutCount = new AtomicInteger(0);
    
    // 耗时统计
    private final AtomicLong totalLatencyMs = new AtomicLong(0);
    private long maxLatencyMs = 0;
    private long minLatencyMs = Long.MAX_VALUE;

    public ToolExecutionStats(String toolName) {
        this.toolName = toolName;
    }

    /**
     * 记录一次成功执行
     */
    public void recordSuccess(long latencyMs) {
        synchronized (this) {
            totalExecutions.incrementAndGet();
            successCount.incrementAndGet();
            totalLatencyMs.addAndGet(latencyMs);
            maxLatencyMs = Math.max(maxLatencyMs, latencyMs);
            minLatencyMs = Math.min(minLatencyMs, latencyMs);
        }
    }

    /**
     * 记录一次失败执行
     */
    public void recordFailure(long latencyMs) {
        synchronized (this) {
            totalExecutions.incrementAndGet();
            failureCount.incrementAndGet();
            totalLatencyMs.addAndGet(latencyMs);
            maxLatencyMs = Math.max(maxLatencyMs, latencyMs);
            minLatencyMs = Math.min(minLatencyMs, latencyMs);
        }
    }

    /**
     * 记录一次超时执行
     */
    public void recordTimeout(long latencyMs) {
        synchronized (this) {
            totalExecutions.incrementAndGet();
            timeoutCount.incrementAndGet();
            totalLatencyMs.addAndGet(latencyMs);
            maxLatencyMs = Math.max(maxLatencyMs, latencyMs);
            minLatencyMs = Math.min(minLatencyMs, latencyMs);
        }
    }

    /**
     * 计算成功率（百分比）
     */
    public double getSuccessRate() {
        int total = totalExecutions.get();
        if (total == 0) return 100.0;
        return (double) successCount.get() / total * 100;
    }

    /**
     * 计算失败率（包括超时）
     */
    public double getFailureRate() {
        int total = totalExecutions.get();
        if (total == 0) return 0.0;
        return (double) (failureCount.get() + timeoutCount.get()) / total * 100;
    }

    /**
     * 计算平均耗时
     */
    public double getAverageLatencyMs() {
        int total = totalExecutions.get();
        if (total == 0) return 0;
        return (double) totalLatencyMs.get() / total;
    }

    /**
     * 判断是否需要告警
     * SLA标准：成功率 >= 95%
     */
    public boolean needsAlert() {
        return getSuccessRate() < 95.0 && totalExecutions.get() >= 10;
    }

    /**
     * 判断是否需要动态调整超时时间
     * 如果p95耗时 > 当前配置的超时时间，应该增加超时
     */
    public boolean shouldIncreaseTimeout(long currentTimeoutMs) {
        // 简化实现：如果最大耗时接近超时时间，则考虑增加
        return maxLatencyMs > currentTimeoutMs * 0.8 && timeoutCount.get() > 5;
    }

    /**
     * 获取建议的新超时时间
     */
    public long getRecommendedTimeoutMs(long currentTimeoutMs) {
        if (shouldIncreaseTimeout(currentTimeoutMs)) {
            // 增加20%
            return (long) (currentTimeoutMs * 1.2);
        }
        return currentTimeoutMs;
    }

    // Getters
    public String getToolName() {
        return toolName;
    }

    public int getTotalExecutions() {
        return totalExecutions.get();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getFailureCount() {
        return failureCount.get();
    }

    public int getTimeoutCount() {
        return timeoutCount.get();
    }

    public long getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public long getMinLatencyMs() {
        return minLatencyMs == Long.MAX_VALUE ? 0 : minLatencyMs;
    }

    @Override
    public String toString() {
        return "ToolExecutionStats{" +
                "toolName='" + toolName + '\'' +
                ", total=" + totalExecutions.get() +
                ", success=" + successCount.get() +
                ", failure=" + failureCount.get() +
                ", timeout=" + timeoutCount.get() +
                ", successRate=" + String.format("%.2f%%", getSuccessRate()) +
                ", avgLatency=" + String.format("%.0fms", getAverageLatencyMs()) +
                ", maxLatency=" + maxLatencyMs + "ms" +
                '}';
    }
}
