package com.example.aiagent.service.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 工具注册表
 * 管理所有可用的 Agent 工具
 */
@Slf4j
@Component
public class ToolRegistry {

    /** 工具缓存，key 为工具名称 */
    private final Map<String, Tool> tools = new HashMap<>();

    /**
     * 构造函数，自动注册所有 Tool 实现
     * @param toolList Spring 自动注入的所有 Tool Bean
     */
    public ToolRegistry(List<Tool> toolList) {
        for (Tool tool : toolList) {
            tools.put(tool.getName(), tool);
            log.info("注册工具: {}", tool.getName());
        }
    }

    /**
     * 根据名称获取工具
     * @param name 工具名称
     * @return 工具实例（如果存在）
     */
    public Optional<Tool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * 获取所有已注册的工具
     * @return 工具映射
     */
    public Map<String, Tool> getAllTools() {
        return Map.copyOf(tools);
    }

    /**
     * 检查工具是否存在
     * @param name 工具名称
     * @return 是否存在
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }
}
