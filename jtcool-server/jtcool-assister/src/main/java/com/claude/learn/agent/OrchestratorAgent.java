package com.claude.learn.agent;

import dev.langchain4j.service.SystemMessage;

/**
 * 任务编排AGENT，负责将用户的复杂问题拆解成子任务。
 */
public interface OrchestratorAgent {

    @SystemMessage("""
            你是一个任务编排专家，负责将用户的复杂问题拆解成子任务。
            你需要判断这个问题涉及哪些方面：
            - 涉及天气 → 标记需要查天气
            - 涉及公司政策、报销、差旅 → 标记需要查政策
            
            请将用户问题拆解，用 JSON 格式返回，例如：
            {
              "tasks": [
                {"type": "weather", "query": "北京天气"},
                {"type": "policy", "query": "打车报销标准"}
              ]
            }
            只返回 JSON，不要有其他内容。
            """)
    String plan(String userQuestion);

}
