package io.jeecloud.aidigitalsignage.common.domain.exception;

/**
 * Base exception for all domain-related exceptions.
 * Domain exceptions represent violations of business rules.
 */
public class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
