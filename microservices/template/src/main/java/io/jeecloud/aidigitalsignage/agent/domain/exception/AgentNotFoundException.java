package io.jeecloud.aidigitalsignage.agent.domain.exception;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;

/**
 * Exception thrown when an agent is not found.
 */
public class AgentNotFoundException extends DomainException {
    
    public AgentNotFoundException(AgentCode agentCode) {
        super("Agent not found with code: " + agentCode.value());
    }
    
    public AgentNotFoundException(String agentCode) {
        super("Agent not found with code: " + agentCode);
    }
}

