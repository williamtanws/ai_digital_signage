package io.jeecloud.aidigitalsignage.user.domain;

import io.jeecloud.aidigitalsignage.common.domain.base.ValueObject;

import java.util.Objects;

/**
 * Value Object representing a User's NRIC (National Registration Identity Card).
 * Provides validation and ensures NRIC format correctness.
 */
public class NewNric implements ValueObject {
    
    private final String value;

    private NewNric(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("NRIC cannot be null or empty");
        }
        
        String trimmed = value.trim();
        
        // Basic NRIC validation (Malaysian format: YYMMDD-PB-XXXG)
        // Example: 850525-14-5678
        if (!trimmed.matches("^\\d{6}-\\d{2}-\\d{4}$")) {
            throw new IllegalArgumentException("Invalid NRIC format. Expected format: YYMMDD-PB-XXXG (e.g., 850525-14-5678)");
        }
        
        this.value = trimmed;
    }

    public static NewNric of(String value) {
        return new NewNric(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewNric newNric = (NewNric) o;
        return Objects.equals(value, newNric.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
