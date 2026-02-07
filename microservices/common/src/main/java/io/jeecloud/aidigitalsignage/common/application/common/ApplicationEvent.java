package io.jeecloud.aidigitalsignage.common.application.common;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all application events.
 * Application events are different from domain events - they represent
 * technical/infrastructure concerns rather than business events.
 */
public abstract class ApplicationEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final String eventType;

    protected ApplicationEvent(String eventType) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.eventType = eventType;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getEventType() {
        return eventType;
    }
}
