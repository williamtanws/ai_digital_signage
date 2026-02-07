package io.jeecloud.aidigitalsignage.common.application.common;

/**
 * Marker interface for commands.
 * All commands should implement this interface to indicate they are write operations.
 * Part of CQRS pattern - separates commands from queries.
 */
public interface Command {
    /**
     * Validates the command.
     * @throws IllegalArgumentException if validation fails
     */
    default void validate() {
        // Default implementation does nothing
        // Concrete commands can override for validation
    }
}
