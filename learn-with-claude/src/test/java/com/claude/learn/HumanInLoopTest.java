package com.claude.learn;

import com.claude.learn.agent.HumanInLoopAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HumanInLoopTest {

    @Autowired
    HumanInLoopAgent humanInLoopAgent;

    @Test
    void testHumanInLoopAgent() throws Exception {

        //测试只读操作
        System.out.println("=== 测试1：只读操作 ===");
        String answer1 = humanInLoopAgent.run("北京今天天气怎么样？");
        System.out.println("回答：" + answer1);
        System.out.println("---");

        // 测试2：危险操作，需要人工确认
        System.out.println("=== 测试2：危险操作 ===");
        String answer2 = humanInLoopAgent.run("帮我发邮件给 boss@company.com 告知今天天气");
        System.out.println("回答：" + answer2);

    }

}
