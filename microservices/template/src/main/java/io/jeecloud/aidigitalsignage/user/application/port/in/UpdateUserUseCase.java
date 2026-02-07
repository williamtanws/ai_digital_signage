package io.jeecloud.aidigitalsignage.user.application.port.in;

import io.jeecloud.aidigitalsignage.common.application.common.Command;
import io.jeecloud.aidigitalsignage.user.domain.User;

/**
 * Input Port (Use Case) for updating an existing User.
 * Part of the Application layer in Hexagonal Architecture.
 */
public interface UpdateUserUseCase {
    
    /**
     * Update an existing user.
     */
    User updateUser(UpdateUserCommand command);
    
    /**
     * Change the active status of a user.
     */
    User changeActiveStatus(ChangeActiveStatusCommand command);
    
    /**
     * Command object for updating a user.
     */
    record UpdateUserCommand(
        String userId,
        String email,
        String name
    ) implements Command {
        public UpdateUserCommand {
            // Validation
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("User ID is required");
            }
        }
        
        @Override
        public void validate() {
            // Already validated in compact constructor
        }
    }
    
    /**
     * Command object for changing user active status.
     */
    record ChangeActiveStatusCommand(
        String userId,
        boolean status
    ) implements Command {
        public ChangeActiveStatusCommand {
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("User ID is required");
            }
        }
        
        @Override
        public void validate() {
            // Already validated in compact constructor
        }
    }
}
