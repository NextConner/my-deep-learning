package com.example.aiagent.service.tool;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 获取当前时间工具
 * 返回系统当前日期和时间
 */
@Component
public class GetTimeTool implements Tool {

    @Override
    public String getName() {
        return "get_current_time";
    }

    @Override
    public String getDescription() {
        return "获取当前日期和时间。当用户询问当前时间或日期时使用此工具。";
    }

    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(),
                "required", java.util.List.of()
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return ToolResult.ok("当前时间: " + formatted);
    }
}
