package io.jeecloud.aidigitalsignage.agent.application.port.in;

import io.jeecloud.aidigitalsignage.common.application.common.Command;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;

import java.util.UUID;

/**
 * Input Port (Use Case) for updating an existing Agent.
 */
public interface UpdateAgentUseCase {
    
    /**
     * Update an existing agent.
     */
    Agent updateAgent(UpdateAgentCommand command);
    
    /**
     * Change agent active status.
     */
    Agent changeActiveStatus(ChangeActiveStatusCommand command);
    
    /**
     * Command object for updating an agent.
     */
    record UpdateAgentCommand(
        String agentCode,
        String name,
        String branchCode,
        Channel channel
    ) implements Command {
        public UpdateAgentCommand {
            if (agentCode == null || agentCode.isBlank()) {
                throw new IllegalArgumentException("Agent code is required");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (branchCode == null || branchCode.isBlank()) {
                throw new IllegalArgumentException("Branch code is required");
            }
            if (channel == null) {
                throw new IllegalArgumentException("Channel is required");
            }
        }
        
        @Override
        public void validate() {
            // Already validated in compact constructor
        }
    }
    
    /**
     * Command object for changing agent active status.
     */
    record ChangeActiveStatusCommand(
        String agentCode,
        Boolean status
    ) implements Command {
        public ChangeActiveStatusCommand {
            if (agentCode == null || agentCode.isBlank()) {
                throw new IllegalArgumentException("Agent code is required");
            }
            if (status == null) {
                throw new IllegalArgumentException("Status is required");
            }
        }
        
        @Override
        public void validate() {
            // Already validated in compact constructor
        }
    }
}

