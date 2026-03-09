package com.claude.learn.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具执行监控服务
 *
 * V3优化：动态监控工具执行统计，基于SLA自动告警和建议调整
 *
 * 功能：
 * 1. 定期检查所有工具的执行统计
 * 2. 识别失败率过高的工具
 * 3. 建议调整超时时间
 * 4. 提供实时的监控仪表板数据
 *
 * 面试要点：
 * - 如何定义SLA？成功率应该>95%
 * - 什么时候触发告警？失败率>30%或超时数>5
 * - 是否自动调整？生产环境应该先告警+建议，让运维确认后再调整
 * - 如何与监控系统集成？可输出到Prometheus、DataDog等
 */
@Service
public class ToolExecutionMonitor {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionMonitor.class);
    private static final double SLA_SUCCESS_RATE = 95.0;  // 95% success rate SLA
    private static final int ALERT_THRESHOLD_EXECUTIONS = 10;  // 至少执行10次才告警

    private final ToolExecutionWrapper toolExecutionWrapper;

    public ToolExecutionMonitor(ToolExecutionWrapper toolExecutionWrapper) {
        this.toolExecutionWrapper = toolExecutionWrapper;
    }

    /**
     * 检查所有工具的健康状态
     */
    public Map<String, Object> checkHealth() {
        Map<String, ToolExecutionStats> allStats = toolExecutionWrapper.getAllStats();
        Map<String, Object> result = new HashMap<>();
        
        boolean healthy = true;
        int alertCount = 0;
        
        for (ToolExecutionStats stats : allStats.values()) {
            if (stats.needsAlert()) {
                healthy = false;
                alertCount++;
                
                // 生成告警
                String alertMessage = String.format(
                    "Tool '%s' has low success rate: %.2f%% (failures: %d, timeouts: %d)",
                    stats.getToolName(),
                    stats.getSuccessRate(),
                    stats.getFailureCount(),
                    stats.getTimeoutCount()
                );
                log.warn(alertMessage);
            }
        }
        
        result.put("healthy", healthy);
        result.put("alertCount", alertCount);
        result.put("statistics", allStats);
        
        return result;
    }

    /**
     * 获取建议的配置调整
     */
    public Map<String, Object> getRecommendations() {
        Map<String, ToolExecutionStats> allStats = toolExecutionWrapper.getAllStats();
        Map<String, Object> recommendations = new HashMap<>();
        
        for (ToolExecutionStats stats : allStats.values()) {
            // 检查是否需要增加超时时间
            ToolRetryStrategy currentStrategy = ToolRetryStrategy.forTool(stats.getToolName());
            long recommendedTimeout = stats.getRecommendedTimeoutMs(currentStrategy.getTimeoutMs());
            
            if (recommendedTimeout > currentStrategy.getTimeoutMs()) {
                Map<String, Object> toolRecommendation = new HashMap<>();
                toolRecommendation.put("currentTimeout", currentStrategy.getTimeoutMs());
                toolRecommendation.put("recommendedTimeout", recommendedTimeout);
                toolRecommendation.put("reason", "High max latency: " + stats.getMaxLatencyMs() + "ms");
                toolRecommendation.put("maxLatency", stats.getMaxLatencyMs());
                toolRecommendation.put("avgLatency", String.format("%.0f", stats.getAverageLatencyMs()));
                
                recommendations.put(stats.getToolName(), toolRecommendation);
            }
        }
        
        return recommendations;
    }

    /**
     * 获取实时监控仪表板数据
     */
    public Map<String, Map> getDashboard() {
        Map<String, ToolExecutionStats> allStats = toolExecutionWrapper.getAllStats();
        Map<String, Map> dashboard = new HashMap<>();
        
        for (ToolExecutionStats stats : allStats.values()) {
            Map<String, Object> toolMetrics = new HashMap<>();
            toolMetrics.put("totalExecutions", stats.getTotalExecutions());
            toolMetrics.put("successCount", stats.getSuccessCount());
            toolMetrics.put("failureCount", stats.getFailureCount());
            toolMetrics.put("timeoutCount", stats.getTimeoutCount());
            toolMetrics.put("successRate", String.format("%.2f%%", stats.getSuccessRate()));
            toolMetrics.put("failureRate", String.format("%.2f%%", stats.getFailureRate()));
            toolMetrics.put("avgLatencyMs", String.format("%.0f", stats.getAverageLatencyMs()));
            toolMetrics.put("maxLatencyMs", stats.getMaxLatencyMs());
            toolMetrics.put("minLatencyMs", stats.getMinLatencyMs());
            toolMetrics.put("needsAlert", stats.needsAlert());
            
            dashboard.put(stats.getToolName(), toolMetrics);
        }
        
        return dashboard;
    }

    /**
     * 重置所有统计数据（用于测试或维护）
     */
    public void resetStats(String toolName) {
        Map<String, ToolExecutionStats> allStats = toolExecutionWrapper.getAllStats();
        ToolExecutionStats stats = allStats.get(toolName);
        if (stats != null) {
            log.info("Resetting statistics for tool: {}", toolName);
            // 注意：当前实现中ToolExecutionStats没有reset方法，
            // 如果需要重置需要创建新的实例或添加reset方法
        }
    }
}
