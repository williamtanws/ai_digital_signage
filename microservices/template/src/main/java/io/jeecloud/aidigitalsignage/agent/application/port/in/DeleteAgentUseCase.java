package io.jeecloud.aidigitalsignage.agent.application.port.in;

import java.util.UUID;

/**
 * Input Port (Use Case) for deleting an Agent.
 */
public interface DeleteAgentUseCase {
    
    /**
     * Delete an agent by ID.
     */
    void deleteAgent(UUID agentId);
}

