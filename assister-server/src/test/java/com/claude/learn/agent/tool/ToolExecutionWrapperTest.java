package com.claude.learn.agent.tool;

import com.claude.learn.agent.AgentTools;
import com.claude.learn.config.AgentRuntimeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToolExecutionWrapperTest {

    @Mock
    private AgentTools agentTools;

    @Mock
    private AgentRuntimeProperties runtimeProperties;

    private ToolExecutionWrapper toolExecutionWrapper;

    @BeforeEach
    void setUp() {
        when(runtimeProperties.getRetryTimes()).thenReturn(1);
        when(runtimeProperties.getStepTimeoutMs()).thenReturn(5000L);
        toolExecutionWrapper = new ToolExecutionWrapper(agentTools, runtimeProperties);
    }

    @Test
    void testExecuteSuccess() {
        // Given
        when(agentTools.getWeather(anyString())).thenReturn("Sunny, 25°C");

        // When
        ToolExecutionResult result = toolExecutionWrapper.execute("getWeather", "{\"city\":\"Beijing\"}");

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("25"));
        assertNull(result.getErrorMessage());
        verify(agentTools, times(1)).getWeather(anyString());
    }

    @Test
    void testExecuteFailure() {
        // Given
        when(agentTools.getWeather(anyString())).thenThrow(new RuntimeException("API error"));

        // When
        ToolExecutionResult result = toolExecutionWrapper.execute("getWeather", "{\"city\":\"Beijing\"}");

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getOutput());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("failed") || result.getErrorMessage().contains("API error"));
        verify(agentTools, times(2)).getWeather(anyString()); // Initial attempt + 1 retry
    }

    @Test
    void testExecuteUnknownTool() {
        // When
        ToolExecutionResult result = toolExecutionWrapper.execute("unknownTool", "{}");

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getOutput());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Unknown tool"));
    }

    @Test
    void testExecuteWithRetry() {
        // Given
        when(agentTools.searchPolicy(anyString()))
                .thenThrow(new RuntimeException("Temporary error"))
                .thenReturn("Policy document found");

        // When
        ToolExecutionResult result = toolExecutionWrapper.execute("searchPolicy", "{\"query\":\"travel\"}");

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getOutput());
        assertEquals("Policy document found", result.getOutput());
        verify(agentTools, times(2)).searchPolicy(anyString()); // Failed once, succeeded on retry
    }
}

