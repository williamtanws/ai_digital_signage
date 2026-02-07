package io.jeecloud.aidigitalsignage.common.domain;

import io.jeecloud.aidigitalsignage.common.domain.base.ValueObject;

/**
 * Value Object representing an Agent's unique code.
 * 
 * <p>AgentCode is immutable and self-validating, ensuring that all instances
 * have a valid code that meets business rules. This is part of the Agent domain
 * and encapsulates the validation logic for agent codes.</p>
 * 
 * @param value the agent code string (minimum 6 characters)
 */
public record AgentCode(String value) implements ValueObject {
    
    /**
     * Validates the agent code on construction.
     * 
     * @param value the agent code string
     * @throws IllegalArgumentException if value is null or less than 6 characters
     */
    public AgentCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Agent code cannot be null or empty");
        }
        if (value.length() < 6) {
            throw new IllegalArgumentException("Agent code must be at least 6 characters");
        }
    }
    
    /**
     * Factory method to create an AgentCode from a string.
     * 
     * @param code the agent code string
     * @return a new AgentCode instance
     */
    public static AgentCode of(String code) {
        return new AgentCode(code);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
