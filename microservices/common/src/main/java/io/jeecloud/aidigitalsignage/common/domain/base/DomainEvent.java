package io.jeecloud.aidigitalsignage.common.domain.base;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all Domain Events.
 * Domain events represent something that happened in the domain that is important to the business.
 */
public interface DomainEvent {
    UUID getEventId();
    Instant getOccurredOn();
    String getEventType();
}
