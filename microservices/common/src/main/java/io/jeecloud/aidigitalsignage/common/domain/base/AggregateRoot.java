package io.jeecloud.aidigitalsignage.common.domain.base;

import java.util.List;

/**
 * Base interface for all Aggregate Roots.
 * Aggregate Roots are entities that control access to other entities and value objects.
 * They maintain consistency boundaries and are the only objects that can be accessed directly.
 */
public interface AggregateRoot<ID> extends Entity<ID> {
    /**
     * Returns domain events that occurred during the lifecycle of this aggregate.
     */
    List<DomainEvent> getDomainEvents();
    
    /**
     * Clears all domain events after they have been published.
     */
    void clearDomainEvents();
}
