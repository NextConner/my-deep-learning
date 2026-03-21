package com.claude.learn;

import com.claude.learn.agent.runtime.AgentErrorCode;

public class AgentBusinessException extends Throwable {
    public AgentBusinessException(AgentErrorCode agentErrorCode, String resMsg) {
    }
}
