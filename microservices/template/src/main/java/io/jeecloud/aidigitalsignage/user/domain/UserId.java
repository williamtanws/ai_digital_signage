package io.jeecloud.aidigitalsignage.user.domain;

import io.jeecloud.aidigitalsignage.common.domain.base.ValueObject;

import java.util.Objects;

/**
 * Value Object representing a User's unique identifier.
 * Uses String for user identification (typically username).
 */
public class UserId implements ValueObject {
    
    private final String value;

    private UserId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        this.value = value;
    }

    public static UserId generate() {
        throw new UnsupportedOperationException("UserId must be explicitly provided, cannot be auto-generated");
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
