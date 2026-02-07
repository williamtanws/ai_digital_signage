package io.jeecloud.aidigitalsignage.common.domain.base;

/**
 * Base interface for all Entities.
 * Entities have identity and their equality is based on their ID, not attributes.
 */
public interface Entity<ID> {
    /**
     * Returns the unique identifier of the entity.
     */
    ID getId();
}
