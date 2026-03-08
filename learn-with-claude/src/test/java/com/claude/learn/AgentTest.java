package com.claude.learn;

import com.claude.learn.agent.PolicyAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AgentTest {

    @Autowired
    private PolicyAgent policyAgent;


    @Test
    public void testAgent(){

        // 测试1：只需要 RAG 工具
//        System.out.println("=== 测试1 ===");
//        String answer1 = policyAgent.chat("飞机票商务舱怎么报销？然后需要住酒店呢？报销1000？");
//        System.out.println("回答：" + answer1);
//        System.out.println("---");
//
//        // 测试2：只需要天气工具
//        System.out.println("=== 测试2 ===");
//        String answer2 = policyAgent.chat("北京今天天气怎么样？");
//        System.out.println("回答：" + answer2);
//        System.out.println("---");

        // 测试3：需要同时调用两个工具
        System.out.println("=== 测试3 ===");
        String answer3 = policyAgent.chat("我想现在从北京打车到江苏上海，报销上限是多少？", "你是一个企业内部智能助手");
        System.out.println("回答：" + answer3);

    }

}
