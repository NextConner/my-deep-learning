package com.claude.learn.agent;

import com.claude.learn.agent.tool.ToolExecutionResult;
import com.claude.learn.agent.tool.ToolExecutionWrapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class HumanInLoopAgent {

    private static final Logger log = LoggerFactory.getLogger(HumanInLoopAgent.class);
    private static final Scanner scanner = new Scanner(System.in);

    // 需要人工确认的危险工具
    private static final List<String> DANGEROUS_TOOLS = List.of(
            "sendEmail", "deleteData", "transferMoney"
    );

    private final ChatLanguageModel chatLanguageModel;
    private final AgentTools agentTools;
    private final ToolExecutionWrapper toolExecutionWrapper;

    public HumanInLoopAgent(ChatLanguageModel chatLanguageModel, AgentTools agentTools, ToolExecutionWrapper toolExecutionWrapper) {
        this.chatLanguageModel = chatLanguageModel;
        this.agentTools = agentTools;
        this.toolExecutionWrapper = toolExecutionWrapper;
    }

    public String run(String userInput) throws Exception {
        //初始化历史消息
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from("""
                你是企业内部智能助手，可以调用工具完成任务。
                只读操作直接执行，涉及发送邮件、删除数据、转账等操作需要人工确认。
                """));
        messages.add(UserMessage.from(userInput));
        AgentTools rawTarget = (AgentTools) ((Advised) agentTools).getTargetSource().getTarget();

        //获取工具列表
        List<ToolSpecification> tools = ToolSpecifications.toolSpecificationsFrom(rawTarget);


        //模拟简单的agent 循环
        while (true) {
            // LLM 决策
            Response<AiMessage> response = chatLanguageModel.generate(messages, tools);
            AiMessage aiMessage = response.content();
            messages.add(aiMessage);

            //
            if(!aiMessage.hasToolExecutionRequests()){
                log.info("任务完成！");
                return  aiMessage.text();
            }

            //有工具调用 -> 逐个处理
            for (var toolRequest : aiMessage.toolExecutionRequests()) {
                String toolName = toolRequest.name();
                String toolArgs = toolRequest.arguments().toString();

                log.info("");log.info("🔧 Agent 想调用工具：{}，参数：{}", toolName, toolArgs);
                //判断是否需要人工确认
                if(DANGEROUS_TOOLS.contains(toolName)){
                    System.out.println("\n⚠️  Agent 请求执行敏感操作：");
                    System.out.println("   工具：" + toolName);
                    System.out.println("   参数：" + toolArgs);
                    System.out.print("   是否允许执行？(y/n)：");

                    String confirm = scanner.nextLine().trim();
                    if(!confirm.equalsIgnoreCase("Y")){
                        //拒绝执行，并告知LLM
                        messages.add(ToolExecutionResultMessage.from(toolRequest,"用户拒绝执行此操作"));
                        log.info("❌ 用户拒绝执行：{}", toolName);
                        continue;
                    }
                }

                // 执行工具
                ToolExecutionResult result = toolExecutionWrapper.execute(toolName, toolArgs);

                if (result.isSuccess()) {
                    log.info("   工具返回：{}", result.getOutput());
                    messages.add(ToolExecutionResultMessage.from(toolRequest, result.getOutput()));
                } else {
                    log.error("   工具执行失败：{}", result.getErrorMessage());
                    messages.add(ToolExecutionResultMessage.from(toolRequest, "工具执行失败：" + result.getErrorMessage()));
                }
            }

        }

    }

}

