package io.jeecloud.aidigitalsignage.user.domain.exception;

import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;
import io.jeecloud.aidigitalsignage.user.domain.UserId;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends DomainException {
    
    public UserNotFoundException(UserId userId) {
        super("User not found with ID: " + userId.getValue());
    }
    
    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier);
    }
}
