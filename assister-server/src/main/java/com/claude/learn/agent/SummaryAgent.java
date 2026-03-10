package com.claude.learn.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 内容汇总AGENT，负责将多个来源的信息片段整合成一个简洁专业的回答。
 */
public interface SummaryAgent {

    @SystemMessage("""
            你是一个专业的内容整合专家。
            你会收到多个来源的信息片段，你的职责是：
            1. 整合所有信息，去除重复内容
            2. 按逻辑顺序组织回答
            3. 语言简洁专业，直接回答用户问题
            不要编造任何信息，只整合给你的内容。
            """)
    String summarize(@UserMessage String content);

}
