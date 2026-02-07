package io.jeecloud.aidigitalsignage.agent.application.port.in;

import io.jeecloud.aidigitalsignage.common.application.common.Command;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;

/**
 * Input Port (Use Case) for creating a new Agent.
 * Part of the Application layer in Hexagonal Architecture.
 */
public interface CreateAgentUseCase {
    
    /**
     * Create a new agent.
     */
    Agent createAgent(CreateAgentCommand command);
    
    /**
     * Command object for creating an agent.
     */
    record CreateAgentCommand(
        String agentCode,
        String name,
        String branchCode,
        Channel channel
    ) implements Command {
        public CreateAgentCommand {
            // Validation can be added here
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
        
        public AgentCode toAgentCode() {
            return AgentCode.of(agentCode);
        }
        
        @Override
        public void validate() {
            // Already validated in compact constructor
        }
    }
}

