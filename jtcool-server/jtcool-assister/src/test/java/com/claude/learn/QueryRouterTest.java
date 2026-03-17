package com.claude.learn;



import com.claude.learn.filter.LocalQueryRouter;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QueryRouter 准确率测试
 *
 * 测试策略：
 * - YES 用例：应该走 RAG 检索的问题（企业内部知识）
 * - NO 用例：应该直接回答的问题（通用知识、闲聊）
 * - 边界用例：容易判断错的模糊问题
 *
 * 准确率目标：>= 90%
 */
@SpringBootTest
class QueryRouterTest {

    @Autowired
    @Qualifier("ollamaModel")
    private ChatLanguageModel ollamaModel;

    private LocalQueryRouter router;

    @BeforeEach
    void setUp() {
        router = new LocalQueryRouter(ollamaModel);
    }

    // -------------------------------------------------------------------------
    // 测试数据定义
    // -------------------------------------------------------------------------

    /**
     * 应该返回 YES 的用例（需要查企业知识库）
     */
    static Stream<TestCase> yesTestCases() {
        return Stream.of(
                // 人事 / HR 类
                new TestCase("年假有几天？", true),
                new TestCase("请假需要提前几天申请？", true),
                new TestCase("病假工资怎么算？", true),
                new TestCase("产假政策是什么？", true),
                new TestCase("员工离职流程是什么？", true),

                // 报销 / 财务类
                new TestCase("出差打车费用可以报销吗？", true),
                new TestCase("餐饮报销上限是多少？", true),
                new TestCase("发票报销需要几个工作日到账？", true),
                new TestCase("出差住宿标准是多少？", true),

                // 流程 / 规范类
                new TestCase("代码上线需要走什么审批流程？", true),
                new TestCase("新员工入职需要准备哪些材料？", true),
                new TestCase("项目立项需要哪些部门审批？", true),
                new TestCase("服务器申请流程是怎样的？", true),

                // 内部系统类
                new TestCase("OA系统在哪里登录？", true),
                new TestCase("内部wiki怎么访问？", true),
                new TestCase("公司VPN怎么配置？", true)
        );
    }

    /**
     * 应该返回 NO 的用例（通用知识，直接回答）
     */
    static Stream<TestCase> noTestCases() {
        return Stream.of(
                // 编程 / 技术类
                new TestCase("Spring Boot 怎么配置数据源？", false),
                new TestCase("Java 中 HashMap 和 TreeMap 的区别是什么？", false),
                new TestCase("什么是 RAG？", false),
                new TestCase("Docker 怎么构建镜像？", false),
                new TestCase("Redis 和 Memcached 有什么区别？", false),

                // 闲聊 / 问候类
                new TestCase("你好", false),
                new TestCase("今天天气怎么样？", false),
                new TestCase("你是谁？", false),
                new TestCase("讲个笑话吧", false),

                // 通用知识类
                new TestCase("中国的首都是哪里？", false),
                new TestCase("牛顿第一定律是什么？", false),
                new TestCase("二战是哪年结束的？", false),

                // 写代码 / 生成内容类
                new TestCase("帮我写一个排序算法", false),
                new TestCase("帮我写一封邮件", false),
                new TestCase("解释一下什么是微服务架构", false)
        );
    }

    /**
     * 边界用例（容易误判，重点关注）
     */
    static Stream<TestCase> borderlineTestCases() {
        return Stream.of(
                // 问题模糊，但应该查知识库
                new TestCase("我可以带家属参加年会吗？", true),
                new TestCase("试用期多长？", true),
                new TestCase("绩效考核怎么打分？", true),

                // 看起来像内部问题，但其实是通用知识
                new TestCase("企业微服务架构怎么设计？", false),
                new TestCase("如何提高团队效率？", false)
        );
    }

    // -------------------------------------------------------------------------
    // 测试用例
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "[YES] {0}")
    @MethodSource("yesTestCases")
    @DisplayName("应该触发 RAG 检索的问题")
    void shouldReturnTrueForEnterpriseQuestions(TestCase testCase) {
        boolean result = router.needsRetrieval(testCase.question());
        assertThat(result)
                .as("问题 [%s] 应该返回 yes（需要检索），但实际返回了 no", testCase.question())
                .isTrue();
    }

    @ParameterizedTest(name = "[NO] {0}")
    @MethodSource("noTestCases")
    @DisplayName("不需要 RAG 检索的问题")
    void shouldReturnFalseForGeneralQuestions(TestCase testCase) {
        boolean result = router.needsRetrieval(testCase.question());
        assertThat(result)
                .as("问题 [%s] 应该返回 no（直接回答），但实际返回了 yes", testCase.question())
                .isFalse();
    }

    @ParameterizedTest(name = "[边界] {0}")
    @MethodSource("borderlineTestCases")
    @DisplayName("边界用例（重点关注误判率）")
    void borderlineCases(TestCase testCase) {
        boolean result = router.needsRetrieval(testCase.question());
        assertThat(result)
                .as("边界问题 [%s] 期望 %s，实际返回 %s",
                        testCase.question(),
                        testCase.expectedNeedsRetrieval() ? "yes" : "no",
                        result ? "yes" : "no")
                .isEqualTo(testCase.expectedNeedsRetrieval());
    }

    // -------------------------------------------------------------------------
    // 准确率统计测试（整体评估）
    // -------------------------------------------------------------------------

    @org.junit.jupiter.api.Test
    @DisplayName("整体准确率应该 >= 90%")
    void overallAccuracyShouldMeetThreshold() {
        List<TestCase> allCases = Stream.of(
                yesTestCases(),
                noTestCases(),
                borderlineTestCases()
        ).flatMap(s -> s).toList();

        long correct = allCases.stream()
                .filter(tc -> router.needsRetrieval(tc.question()) == tc.expectedNeedsRetrieval())
                .count();

        double accuracy = (double) correct / allCases.size() * 100;

        System.out.printf("=== QueryRouter 准确率报告 ===%n");
        System.out.printf("总用例数：%d%n", allCases.size());
        System.out.printf("正确数：%d%n", correct);
        System.out.printf("准确率：%.1f%%%n", accuracy);

        assertThat(accuracy)
                .as("准确率 %.1f%% 低于目标 90%%", accuracy)
                .isGreaterThanOrEqualTo(90.0);
    }

    // -------------------------------------------------------------------------
    // 测试数据记录类
    // -------------------------------------------------------------------------

    record TestCase(String question, boolean expectedNeedsRetrieval) {
        @Override
        public String toString() {
            return question;
        }
    }
}
