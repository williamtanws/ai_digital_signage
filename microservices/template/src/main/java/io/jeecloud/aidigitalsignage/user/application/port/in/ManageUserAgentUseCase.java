package io.jeecloud.aidigitalsignage.user.application.port.in;

import io.jeecloud.aidigitalsignage.common.application.common.Command;
import io.jeecloud.aidigitalsignage.user.domain.User;

/**
 * Input Port (Use Case) for managing User-Agent associations.
 * Part of the Application layer in Hexagonal Architecture.
 */
public interface ManageUserAgentUseCase {
    
    /**
     * Add an agent association to a user.
     */
    User addAgentToUser(AddAgentCommand command);
    
    /**
     * Remove an agent association from a user.
     */
    User removeAgentFromUser(RemoveAgentCommand command);
    
    /**
     * Command object for adding an agent to a user.
     */
    record AddAgentCommand(
        String userId,
        String agentCode
    ) implements Command {
        public AddAgentCommand {
            // Validation
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (agentCode == null || agentCode.isBlank()) {
                throw new IllegalArgumentException("Agent code is required");
            }
        }
        
        @Override
        public void validate() {
            // Already validated in compact constructor
        }
    }
    
    /**
     * Command object for removing an agent from a user.
     */
    record RemoveAgentCommand(
        String userId,
        String agentCode
    ) implements Command {
        public RemoveAgentCommand {
            // Validation
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (agentCode == null || agentCode.isBlank()) {
                throw new IllegalArgumentException("Agent code is required");
            }
        }
        
        @Override
        public void validate() {
            // Already validated in compact constructor
        }
    }
}
