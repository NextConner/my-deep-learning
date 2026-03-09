package com.claude.learn.agent.runtime;

public enum AgentErrorCode {
    NONE,
    TOOL_TIMEOUT,
    TOOL_VALIDATION_FAIL,
    MAX_STEPS_EXCEEDED,
    NO_RELEVANT_CONTEXT, TOOL_EXECUTION_FAIL
}
