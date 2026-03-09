package com.claude.learn.service;

import com.claude.learn.agent.OrchestratorAgent;
import com.claude.learn.agent.PolicyAgent;
import com.claude.learn.agent.runtime.AgentRun;
import com.claude.learn.agent.runtime.AgentRunStatus;
import com.claude.learn.config.AgentRuntimeProperties;
import com.claude.learn.filter.LocalQueryRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
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

    private AgentOrchestratorService orchestratorService;

    @Autowired
    private LocalQueryRouter localQueryRouter;

    @BeforeEach
    void setUp() {
        lenient().when(runtimeProperties.getMaxSteps()).thenReturn(3);
        lenient().when(runtimeProperties.getStepTimeoutMs()).thenReturn(5000L);
        lenient().when(runtimeProperties.getRetryTimes()).thenReturn(1);
        // orchestratorAgent simply echoes the question in a minimal JSON plan
        when(orchestratorAgent.plan(anyString())).thenAnswer(invocation -> {
            String q = invocation.getArgument(0);
            return "{\"tasks\":[{\"type\":\"none\",\"query\":\"" + q + "\"}]}";
        });
        orchestratorService = new AgentOrchestratorService(policyAgent, orchestratorAgent, runtimeProperties,localQueryRouter);
    }

    @Test
    void testRunSuccess() {
        // Given
        when(policyAgent.chat(anyString(), anyString())).thenReturn("The answer is 42");

        // When
        AgentRun run = orchestratorService.run("testuser", "What is the answer?", "You are a helpful assistant");

        // Then
        assertNotNull(run);
        assertEquals(AgentRunStatus.SUCCESS, run.getStatus());
        assertEquals("The answer is 42", run.getFinalAnswer());
        assertEquals("testuser", run.getUsername());
        assertEquals(1, run.getSteps().size());
        assertTrue(run.totalLatencyMs() >= 0); // Latency can be 0 for fast tests
        verify(policyAgent, times(1)).chat(anyString(), anyString());
    }

    @Test
    void testRunFailureMaxStepsExceeded() {
        // Given
        when(policyAgent.chat(anyString(), anyString())).thenReturn(null);

        // When
        AgentRun run = orchestratorService.run("testuser", "What is the answer?", "You are a helpful assistant");

        // Then
        assertNotNull(run);
        assertEquals(AgentRunStatus.FAILED, run.getStatus());
        assertNull(run.getFinalAnswer());
        assertEquals(4, run.getSteps().size()); // 3 attempts + 1 guard step
        // When policyAgent returns null, it doesn't retry within the same step, it
        // moves to the next step
        verify(policyAgent, times(3)).chat(anyString(), anyString()); // 3 steps, no retries because null is not an
                                                                      // exception
    }

    @Test
    void testRunWithRetry() {
        // Given
        when(policyAgent.chat(anyString(), anyString()))
                .thenReturn(null)
                .thenReturn("Success on second step");

        // When
        AgentRun run = orchestratorService.run("testuser", "What is the answer?", "You are a helpful assistant");

        // Then
        assertNotNull(run);
        assertEquals(AgentRunStatus.SUCCESS, run.getStatus());
        assertEquals("Success on second step", run.getFinalAnswer());
        assertEquals(2, run.getSteps().size()); // First step returned null, second step succeeded
        verify(policyAgent, times(2)).chat(anyString(), anyString());
    }

    @Test
    void testRunIdGeneration() {
        // Given
        when(policyAgent.chat(anyString(), anyString())).thenReturn("Answer");

        // When
        AgentRun run1 = orchestratorService.run("user1", "Question 1", "System prompt");
        AgentRun run2 = orchestratorService.run("user2", "Question 2", "System prompt");

        // Then
        assertNotEquals(run1.getRunId(), run2.getRunId());
    }
}
