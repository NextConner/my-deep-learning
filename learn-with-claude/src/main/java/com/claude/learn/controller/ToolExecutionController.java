package com.claude.learn.controller;

import com.claude.learn.agent.tool.ToolExecutionMonitor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 工具执行监控API
 *
 * V3优化：提供实时的监控接口
 *
 * 端点：
 * - GET /api/tools/health - 检查工具健康状态
 * - GET /api/tools/dashboard - 获取监控仪表板
 * - GET /api/tools/recommendations - 获取配置建议
 *
 * 面试要点：
 * - 为什么需要这些接口？便于运维监控系统状态
 * - 如何与APM系统集成？可直接输出Prometheus格式
 * - 是否需要权限控制？生产环境应该限制访问
 */
@RestController
@RequestMapping("/api/tools")
public class ToolExecutionController {

    private final ToolExecutionMonitor toolExecutionMonitor;

    public ToolExecutionController(ToolExecutionMonitor toolExecutionMonitor) {
        this.toolExecutionMonitor = toolExecutionMonitor;
    }

    /**
     * 检查工具健康状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return toolExecutionMonitor.checkHealth();
    }

    /**
     * 获取监控仪表板数据
     */
    @GetMapping("/dashboard")
    public Map<String, Map> dashboard() {
        return toolExecutionMonitor.getDashboard();
    }

    /**
     * 获取配置建议
     */
    @GetMapping("/recommendations")
    public Map<String, Object> recommendations() {
        return toolExecutionMonitor.getRecommendations();
    }

    /**
     * 重置特定工具的统计信息
     */
    @PostMapping("/{toolName}/reset-stats")
    public Map<String, String> resetStats(@PathVariable String toolName) {
        toolExecutionMonitor.resetStats(toolName);
        return Map.of("message", "Statistics reset for tool: " + toolName);
    }
}
