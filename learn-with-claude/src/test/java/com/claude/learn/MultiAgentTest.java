package com.claude.learn;

import com.claude.learn.service.MultiAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MultiAgentTest {

    @Autowired
    private MultiAgentService multiAgentService;

    @Test
    void testMultiAgent() throws Exception {
        // 测试1：单一任务
        System.out.println("=== 测试1：单一任务 ===");
        String answer1 = multiAgentService.run("北京今天天气怎么样？");
        System.out.println("回答：" + answer1);
        System.out.println("---");

        // 测试2：复合任务，验证并发执行
        System.out.println("=== 测试2：复合任务 ===");
        long start = System.currentTimeMillis();
        String answer2 = multiAgentService.run(
                "我要出差去上海，天气怎么样？打车和酒店能报多少？"
        );
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("回答：" + answer2);
        System.out.println("耗时：" + elapsed + " ms");
        System.out.println("---");

        // 测试3：三个子任务并发
        System.out.println("=== 测试3：三个子任务 ===");
        start = System.currentTimeMillis();
        String answer3 = multiAgentService.run(
                "我要分别去北京和上海出差，两个城市天气如何？差旅报销标准是什么？"
        );
        elapsed = System.currentTimeMillis() - start;
        System.out.println("回答：" + answer3);
        System.out.println("耗时：" + elapsed + " ms");

    }


}
