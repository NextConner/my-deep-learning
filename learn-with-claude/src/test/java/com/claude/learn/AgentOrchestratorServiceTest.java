package com.claude.learn;


import com.claude.learn.agent.OrchestratorAgent;
import com.claude.learn.agent.PolicyAgent;
import com.claude.learn.agent.runtime.AgentErrorCode;
import com.claude.learn.agent.runtime.AgentRun;
import com.claude.learn.agent.runtime.AgentRunStatus;
import com.claude.learn.config.AgentRuntimeProperties;
import com.claude.learn.service.AgentOrchestratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * AgentOrchestratorService 分支覆盖率测试
 *
 * 覆盖的关键分支：
 * 1. 多任务 vs 单任务路径
 * 2. 正常成功 vs 失败重试
 * 3. 最后一步成功 vs 超过最大步数
 * 4. 各类错误码处理
 */
@ExtendWith(MockitoExtension.class)
class AgentOrchestratorServiceTest {

    @Mock
    private PolicyAgent policyAgent;

    @Mock
    private OrchestratorAgent orchestratorAgent;

    @Mock
    private AgentRuntimeProperties runtimeProperties;

    @InjectMocks
    private AgentOrchestratorService service;

    @BeforeEach
    void setUp() {
        when(runtimeProperties.getMaxSteps()).thenReturn(3);
        when(runtimeProperties.getRetryTimes()).thenReturn(1);
        when(runtimeProperties.getStepTimeoutMs()).thenReturn(5000L);
    }

    // -------------------------------------------------------------------------
    // 1. 正常成功路径
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("正常成功路径")
    class SuccessPath {

        @Test
        @DisplayName("第一步成功，直接返回结果")
        void shouldSucceedOnFirstStep() {
            when(orchestratorAgent.plan(any())).thenReturn("{\"tasks\":[]}");
            when(policyAgent.chat(any(), any())).thenReturn("正确答案");

            AgentRun run = service.run("user", "请假流程是什么？", "system");

            assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
            assertThat(run.getFinalAnswer()).isEqualTo("正确答案");
            assertThat(run.getSteps()).hasSize(1);
        }

        @Test
        @DisplayName("多任务并行执行成功")
        void shouldSucceedWithMultipleTasks() {
            String plan = """
                    {
                      "tasks": [
                        {"type": "policy", "query": "请假流程"},
                        {"type": "policy", "query": "报销标准"}
                      ]
                    }
                    """;
            when(orchestratorAgent.plan(any())).thenReturn(plan);
            when(policyAgent.chat(any(), any()))
                    .thenReturn("请假需要提前3天申请")
                    .thenReturn("餐饮报销上限100元");

            AgentRun run = service.run("user", "请假和报销怎么操作？", "system");

            assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
            assertThat(run.getFinalAnswer())
                    .contains("请假需要提前3天申请")
                    .contains("餐饮报销上限100元");
        }
    }

    // -------------------------------------------------------------------------
    // 2. 重试路径
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("重试路径")
    class RetryPath {

        @Test
        @DisplayName("第一次失败，第二步成功")
        void shouldSucceedAfterRetry() {
            when(orchestratorAgent.plan(any())).thenReturn("{\"tasks\":[]}");
            when(policyAgent.chat(any(), any()))
                    .thenReturn(null)   // 第一步失败
                    .thenReturn("重试后的答案");  // 第二步成功

            AgentRun run = service.run("user", "问题", "system");

            assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
            assertThat(run.getFinalAnswer()).isEqualTo("重试后的答案");
            assertThat(run.getSteps()).hasSize(2);  // 走了两步
        }

        @Test
        @DisplayName("最后一步成功，步骤数等于 maxSteps")
        void shouldSucceedOnLastStep() {
            when(orchestratorAgent.plan(any())).thenReturn("{\"tasks\":[]}");
            when(policyAgent.chat(any(), any()))
                    .thenReturn(null)
                    .thenReturn(null)
                    .thenReturn("最终答案");  // 第三步（最后一步）才成功

            AgentRun run = service.run("user", "问题", "system");

            assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
            assertThat(run.getFinalAnswer()).isEqualTo("最终答案");
            assertThat(run.getSteps()).hasSize(3);  // 验证走了完整的 maxSteps 步
        }
    }

    // -------------------------------------------------------------------------
    // 3. 失败路径（最关键的分支）
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("失败路径")
    class FailurePath {

        @Test
        @DisplayName("超过最大步数，标记失败并记录正确错误码")
        void shouldFailWhenMaxStepsExceeded() {
            when(orchestratorAgent.plan(any())).thenReturn("{\"tasks\":[]}");
            when(policyAgent.chat(any(), any())).thenReturn(null);  // 每次都失败

            AgentRun run = service.run("user", "问题", "system");

            assertThat(run.getStatus()).isEqualTo(AgentRunStatus.FAILED);
            // 耗时不能超过 maxSteps * 单步超时，验证超时控制生效
            assertThat(run.totalLatencyMs())
                    .isLessThan(runtimeProperties.getMaxSteps() * runtimeProperties.getStepTimeoutMs());
            // 最后一个步骤的错误码必须是 MAX_STEPS_EXCEEDED
            assertThat(run.getSteps().getLast().getErrorCode())
                    .isEqualTo(AgentErrorCode.MAX_STEPS_EXCEEDED);
        }

        @Test
        @DisplayName("知识库无内容，直接返回业务错误不重试")
        void shouldFailFastWhenNoRelevantContext() {
            when(orchestratorAgent.plan(any())).thenReturn("{\"tasks\":[]}");
            when(policyAgent.chat(any(), any()))
                    .thenThrow(new AgentBusinessException(
                            AgentErrorCode.NO_RELEVANT_CONTEXT, "知识库中没有相关内容"));

            AgentRun run = service.run("user", "问题", "system");

            assertThat(run.getStatus()).isEqualTo(AgentRunStatus.FAILED);
            assertThat(run.getSteps().getLast().getErrorCode())
                    .isEqualTo(AgentErrorCode.NO_RELEVANT_CONTEXT);
            // 关键：不可重试错误只走一步，不应该有多次重试
            assertThat(run.getSteps()).hasSize(1);
        }

        @Test
        @DisplayName("超时错误，应该重试")
        void shouldRetryOnTimeout() {
            when(orchestratorAgent.plan(any())).thenReturn("{\"tasks\":[]}");
            when(policyAgent.chat(any(), any()))
                    .thenThrow(new java.util.concurrent.TimeoutException("LLM timeout"))
                    .thenReturn("超时后重试成功");

            AgentRun run = service.run("user", "问题", "system");

            assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
            assertThat(run.getFinalAnswer()).isEqualTo("超时后重试成功");
        }
    }

    // -------------------------------------------------------------------------
    // 4. 边界用例
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("边界用例")
    class EdgeCases {

        @Test
        @DisplayName("orchestrator 返回空任务列表，降级到单步执行")
        void shouldFallbackToSingleStepWhenNoTasks() {
            when(orchestratorAgent.plan(any())).thenReturn("{\"tasks\":[]}");
            when(policyAgent.chat(any(), any())).thenReturn("直接回答");

            AgentRun run = service.run("user", "问题", "system");

            assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        }

        @Test
        @DisplayName("orchestrator 返回非法 JSON，降级到原始问题")
        void shouldFallbackWhenPlanJsonInvalid() {
            when(orchestratorAgent.plan(any())).thenReturn("不是JSON");
            when(policyAgent.chat(any(), any())).thenReturn("降级回答");

            AgentRun run = service.run("user", "问题", "system");

            assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
            assertThat(run.getFinalAnswer()).isEqualTo("降级回答");
        }

        @Test
        @DisplayName("用户名为空，不影响执行")
        void shouldHandleEmptyUsername() {
            when(orchestratorAgent.plan(any())).thenReturn("{\"tasks\":[]}");
            when(policyAgent.chat(any(), any())).thenReturn("答案");

            AgentRun run = service.run("", "问题", "system");

            assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        }
    }
}

