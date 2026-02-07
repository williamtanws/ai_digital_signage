package io.jeecloud.aidigitalsignage.user.application.port.in;

import io.jeecloud.aidigitalsignage.common.application.common.Command;

/**
 * Input Port (Use Case) for deleting a User.
 * Part of the Application layer in Hexagonal Architecture.
 */
public interface DeleteUserUseCase {
    
    /**
     * Delete a user (soft delete - marks as inactive).
     */
    void deleteUser(DeleteUserCommand command);
    
    /**
     * Command object for deleting a user.
     */
    record DeleteUserCommand(String userId) implements Command {
        public DeleteUserCommand {
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
}
