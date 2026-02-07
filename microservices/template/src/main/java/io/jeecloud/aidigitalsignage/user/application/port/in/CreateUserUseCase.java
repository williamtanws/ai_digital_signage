package io.jeecloud.aidigitalsignage.user.application.port.in;

import io.jeecloud.aidigitalsignage.common.application.common.Command;
import io.jeecloud.aidigitalsignage.user.domain.User;

/**
 * Input Port (Use Case) for creating a new User.
 * Part of the Application layer in Hexagonal Architecture.
 */
public interface CreateUserUseCase {
    
    /**
     * Create a new user.
     */
    User createUser(CreateUserCommand command);
    
    /**
     * Command object for creating a user.
     */
    record CreateUserCommand(
        String newNric,
        String email,
        String name
    ) implements Command {
        public CreateUserCommand {
            // Validation
            if (newNric == null || newNric.isBlank()) {
                throw new IllegalArgumentException("NRIC is required");
            }
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name is required");
            }
        }
        
        @Override
        public void validate() {
            // Already validated in compact constructor
        }
    }
}
