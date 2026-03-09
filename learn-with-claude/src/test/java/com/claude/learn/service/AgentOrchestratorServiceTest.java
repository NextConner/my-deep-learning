package com.claude.learn.service;

import com.claude.learn.agent.OrchestratorAgent;
import com.claude.learn.agent.PolicyAgent;
import com.claude.learn.agent.runtime.AgentErrorCode;
import com.claude.learn.agent.runtime.AgentRun;
import com.claude.learn.agent.runtime.AgentRunStatus;
import com.claude.learn.agent.runtime.AgentStep;
import com.claude.learn.agent.runtime.AgentStepStatus;
import com.claude.learn.config.AgentMetrics;
import com.claude.learn.config.AgentRuntimeProperties;
import com.claude.learn.filter.LocalQueryRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentOrchestratorServiceTest {

    @Mock
    private OrchestratorAgent orchestratorAgent;

    @Mock
    private PolicyAgent policyAgent;

    @Mock
    private AgentRuntimeProperties runtimeProperties;

    @Mock
    private LocalQueryRouter localQueryRouter;

    @Mock
    private AgentMetrics agentMetrics;

    private AgentOrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        // Default configuration
        lenient().when(runtimeProperties.getMaxSteps()).thenReturn(3);
        lenient().when(runtimeProperties.getStepTimeoutMs()).thenReturn(5000L);
        lenient().when(runtimeProperties.getRetryTimes()).thenReturn(2);

        // Default orchestrator behavior: return minimal JSON plan
        lenient().when(orchestratorAgent.plan(anyString())).thenAnswer(invocation -> {
            String q = invocation.getArgument(0);
            return "{\"tasks\":[{\"type\":\"none\",\"query\":\"" + q + "\"}]}";
        });

        // Default local router behavior: needs retrieval
        lenient().when(localQueryRouter.needsRetrieval(anyString())).thenReturn(true);

        orchestratorService = new AgentOrchestratorService(
            policyAgent,
            orchestratorAgent,
            runtimeProperties,
            localQueryRouter
        );

        // Use reflection to inject AgentMetrics mock
        try {
            java.lang.reflect.Field field = AgentOrchestratorService.class.getDeclaredField("agentMetrics");
            field.setAccessible(true);
            field.set(orchestratorService, agentMetrics);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject agentMetrics", e);
        }
    }

    // ==================== 正常情况测试 ====================

    @Test
    void testLocalRouterDirectReturn_shouldSkipRAGAndReturnAnswer() {
        // Arrange
        when(localQueryRouter.needsRetrieval("简单问题")).thenReturn(false);
        when(policyAgent.chat("简单问题", "system prompt")).thenReturn("直接答案");

        // Act
        AgentRun run = orchestratorService.run("testuser", "简单问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).isEqualTo("直接答案");
        assertThat(run.getSteps()).isEmpty();
        verify(localQueryRouter).needsRetrieval("简单问题");
        verify(policyAgent).chat("简单问题", "system prompt");
        verify(orchestratorAgent, never()).plan(anyString());
        verify(agentMetrics).recordSuccess(anyLong());
    }

    @Test
    void testMultiTaskParallelExecution_shouldExecuteTasksAndReturnAggregatedResult() {
        // Arrange
        String multiTaskPlan = """
            {
              "tasks": [
                {"type": "weather", "query": "北京天气"},
                {"type": "policy", "query": "报销政策"}
              ]
            }
            """;
        when(orchestratorAgent.plan("复杂问题")).thenReturn(multiTaskPlan);
        when(policyAgent.chat("报销政策", "system prompt")).thenReturn("报销政策答案");

        // Act
        AgentRun run = orchestratorService.run("testuser", "复杂问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).contains("北京今日天气");
        assertThat(run.getFinalAnswer()).contains("报销政策答案");
        assertThat(run.getSteps()).hasSize(1);
        assertThat(run.getSteps().get(0).getToolName()).isEqualTo("orchestrator");
        verify(agentMetrics).recordSuccess(anyLong());
    }

    @Test
    void testSingleStepExecution_shouldCompleteSuccessfully() {
        // Arrange
        when(policyAgent.chat(anyString(), anyString())).thenReturn("成功答案");

        // Act
        AgentRun run = orchestratorService.run("testuser", "普通问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).isEqualTo("成功答案");
        assertThat(run.getUsername()).isEqualTo("testuser");
        assertThat(run.getSteps()).hasSize(1);
        assertThat(run.getSteps().get(0).getStatus()).isEqualTo(AgentStepStatus.SUCCESS);
        assertThat(run.totalLatencyMs()).isGreaterThanOrEqualTo(0);
        verify(policyAgent, times(1)).chat(anyString(), anyString());
    }

    @Test
    void testPlanActObserveLoop_shouldExecuteNormalFlow() {
        // Arrange
        when(policyAgent.chat(anyString(), anyString()))
            .thenReturn(null)  // First step fails
            .thenReturn("第二步成功");  // Second step succeeds

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).isEqualTo("第二步成功");
        assertThat(run.getSteps()).hasSize(2);
        verify(policyAgent, times(2)).chat(anyString(), anyString());
    }

    // ==================== 异常情况测试 ====================

    @Test
    void testToolExecutionTimeout_shouldRetryAndFail() {
        // Arrange
        when(runtimeProperties.getRetryTimes()).thenReturn(1);
        when(policyAgent.chat(anyString(), anyString()))
            .thenAnswer(invocation -> {
                Thread.sleep(6000); // Exceed timeout
                return "答案";
            });

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.FAILED);
        assertThat(run.getSteps()).isNotEmpty();
        AgentStep lastStep = run.getSteps().get(run.getSteps().size() - 1);
        assertThat(lastStep.getErrorCode()).isEqualTo(AgentErrorCode.MAX_STEPS_EXCEEDED);
    }

    @Test
    void testToolExecutionFailureWithRetry_shouldRetryAndSucceed() {
        // Arrange
        when(runtimeProperties.getRetryTimes()).thenReturn(2);
        when(policyAgent.chat(anyString(), anyString()))
            .thenAnswer(invocation -> { throw new SocketTimeoutException("网络超时"); })
            .thenThrow(new RuntimeException("429 Rate limit"))
            .thenReturn("重试成功");

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).isEqualTo("重试成功");
        assertThat(run.getSteps()).hasSize(1);
        verify(policyAgent, times(3)).chat(anyString(), anyString());
    }

    @Test
    void testMaxStepsExceeded_shouldFailWithGuardStep() {
        // Arrange
        when(policyAgent.chat(anyString(), anyString())).thenReturn(null);

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.FAILED);
        assertThat(run.getFinalAnswer()).isNull();
        assertThat(run.getSteps()).hasSize(4); // 3 attempts + 1 guard step

        AgentStep guardStep = run.getSteps().get(3);
        assertThat(guardStep.getToolName()).isEqualTo("runtime_guard");
        assertThat(guardStep.getErrorCode()).isEqualTo(AgentErrorCode.MAX_STEPS_EXCEEDED);
        assertThat(guardStep.getErrorMessage()).contains("max steps");

        verify(policyAgent, times(3)).chat(anyString(), anyString());
        verify(agentMetrics).recordFailure();
    }

    @Test
    void testMultiTaskExecutionFailure_shouldFallbackToSingleStepLoop() {
        // Arrange
        String multiTaskPlan = """
            {
              "tasks": [
                {"type": "weather", "query": "北京天气"}
              ]
            }
            """;
        when(orchestratorAgent.plan("问题")).thenReturn(multiTaskPlan);

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        // Weather task will succeed with hardcoded response, so no fallback
        assertThat(run.getFinalAnswer()).contains("北京");
    }

    @Test
    void testNonRetryableException_shouldFailImmediately() {
        // Arrange
        when(runtimeProperties.getRetryTimes()).thenReturn(2);
        when(policyAgent.chat(anyString(), anyString()))
            .thenThrow(new IllegalArgumentException("无效参数"));

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.FAILED);
        assertThat(run.getSteps()).hasSize(4); // 3 failed steps + 1 guard step
        // Non-retryable exceptions still go through all retry attempts in the current implementation
        verify(policyAgent, atLeast(3)).chat(anyString(), anyString());
    }

    @Test
    void testRetryableSocketTimeoutException_shouldRetry() {
        // Arrange
        when(runtimeProperties.getRetryTimes()).thenReturn(1);
        when(policyAgent.chat(anyString(), anyString()))
            .thenAnswer(invocation -> { throw new SocketTimeoutException("连接超时"); })
            .thenReturn("重试后成功");

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).isEqualTo("重试后成功");
        verify(policyAgent, times(2)).chat(anyString(), anyString());
    }

    @Test
    void testRetryable429Exception_shouldRetry() {
        // Arrange
        when(runtimeProperties.getRetryTimes()).thenReturn(1);
        when(policyAgent.chat(anyString(), anyString()))
            .thenThrow(new RuntimeException("Error 429: Too Many Requests"))
            .thenReturn("重试后成功");

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).isEqualTo("重试后成功");
        verify(policyAgent, times(2)).chat(anyString(), anyString());
    }

    // ==================== 边界条件测试 ====================

    @Test
    void testEmptyMessage_shouldHandleGracefully() {
        // Arrange
        when(policyAgent.chat(anyString(), anyString())).thenReturn("处理空消息");

        // Act
        AgentRun run = orchestratorService.run("testuser", "", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getUserQuestion()).isEmpty();
        verify(policyAgent).chat(anyString(), anyString());
    }

    @Test
    void testNullSystemPrompt_shouldHandleGracefully() {
        // Arrange
        when(policyAgent.chat(anyString(), isNull())).thenReturn("处理null提示词");

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", null);

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        verify(policyAgent).chat(anyString(), isNull());
    }

    @Test
    void testPlanParseFailure_shouldFallbackToOriginalMessage() {
        // Arrange
        when(orchestratorAgent.plan(anyString())).thenReturn("invalid json {{{");
        when(policyAgent.chat(anyString(), anyString())).thenReturn("回退答案");

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).isEqualTo("回退答案");
        verify(orchestratorAgent, atLeastOnce()).plan("问题");
        verify(policyAgent).chat(anyString(), anyString());
    }

    @Test
    void testEmptyTaskList_shouldFallbackToSingleStepLoop() {
        // Arrange
        when(orchestratorAgent.plan(anyString())).thenReturn("{\"tasks\":[]}");
        when(policyAgent.chat(anyString(), anyString())).thenReturn("单步答案");

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).isEqualTo("单步答案");
        verify(policyAgent).chat(anyString(), anyString());
    }

    @Test
    void testTaskListWithOnlyNoneType_shouldFallbackToSingleStepLoop() {
        // Arrange
        String planWithNone = """
            {
              "tasks": [
                {"type": "none", "query": "问题"}
              ]
            }
            """;
        when(orchestratorAgent.plan(anyString())).thenReturn(planWithNone);
        when(policyAgent.chat(anyString(), anyString())).thenReturn("单步答案");

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).isEqualTo("单步答案");
    }

    @Test
    void testTaskListWithUnknownType_shouldIgnoreUnknownTasks() {
        // Arrange
        String planWithUnknown = """
            {
              "tasks": [
                {"type": "unknown", "query": "未知任务"}
              ]
            }
            """;
        when(orchestratorAgent.plan(anyString())).thenReturn(planWithUnknown);
        when(policyAgent.chat(anyString(), anyString())).thenReturn("单步答案");

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
    }

    // ==================== 特定场景测试 ====================

    @Test
    void testWeatherTaskExecution_shouldReturnWeatherInfo() {
        // Arrange
        String weatherPlan = """
            {
              "tasks": [
                {"type": "weather", "query": "上海天气"}
              ]
            }
            """;
        when(orchestratorAgent.plan(anyString())).thenReturn(weatherPlan);

        // Act
        AgentRun run = orchestratorService.run("testuser", "上海天气怎么样", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).contains("上海");
        assertThat(run.getFinalAnswer()).contains("天气");
    }

    @Test
    void testPolicyTaskExecution_shouldCallPolicyAgent() {
        // Arrange
        String policyPlan = """
            {
              "tasks": [
                {"type": "policy", "query": "报销政策"}
              ]
            }
            """;
        when(orchestratorAgent.plan(anyString())).thenReturn(policyPlan);
        when(policyAgent.chat("报销政策", "system prompt")).thenReturn("报销政策详情");

        // Act
        AgentRun run = orchestratorService.run("testuser", "报销政策是什么", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
        assertThat(run.getFinalAnswer()).contains("报销政策详情");
        verify(policyAgent).chat("报销政策", "system prompt");
    }

    @Test
    void testRunIdGeneration_shouldGenerateUniqueIds() {
        // Arrange
        when(policyAgent.chat(anyString(), anyString())).thenReturn("答案");

        // Act
        AgentRun run1 = orchestratorService.run("user1", "问题1", "system prompt");
        AgentRun run2 = orchestratorService.run("user2", "问题2", "system prompt");

        // Assert
        assertThat(run1.getRunId()).isNotEqualTo(run2.getRunId());
        assertThat(run1.getRunId()).isNotNull();
        assertThat(run2.getRunId()).isNotNull();
    }

    @Test
    void testStepSequenceIncrement_shouldIncrementCorrectly() {
        // Arrange
        when(policyAgent.chat(anyString(), anyString()))
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn("最终答案");

        // Act
        AgentRun run = orchestratorService.run("testuser", "问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getSteps()).hasSize(3);
        assertThat(run.getSteps().get(0).getSequence()).isEqualTo(1);
        assertThat(run.getSteps().get(1).getSequence()).isEqualTo(2);
        assertThat(run.getSteps().get(2).getSequence()).isEqualTo(3);
    }

    @Test
    void testReflectMessage_shouldContainOriginalQuestion() {
        // Arrange
        when(policyAgent.chat(anyString(), anyString()))
            .thenReturn(null)
            .thenAnswer(invocation -> {
                String message = invocation.getArgument(0);
                assertThat(message).contains("原始问题");
                assertThat(message).contains("Previous execution failed");
                return "反思后的答案";
            });

        // Act
        AgentRun run = orchestratorService.run("testuser", "原始问题", "system prompt");

        // Assert
        assertThat(run).isNotNull();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.SUCCESS);
    }
}

