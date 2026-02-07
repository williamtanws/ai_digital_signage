package io.jeecloud.aidigitalsignage.user.domain.event;

import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a User is deleted.
 */
public class UserDeletedEvent implements DomainEvent {
    private final UUID eventId;
    private final String userId;
    private final String newNric;
    private final Instant occurredOn;

    public UserDeletedEvent(
        String userId,
        String newNric,
        Instant occurredOn
    ) {
        this.eventId = UUID.randomUUID();
        this.userId = userId;
        this.newNric = newNric;
        this.occurredOn = occurredOn;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }

    @Override
    public String getEventType() {
        return "UserDeleted";
    }

    public String getUserId() {
        return userId;
    }

    public String getNewNric() {
        return newNric;
    }

    @Override
    public String toString() {
        return "UserDeletedEvent{" +
            "eventId=" + eventId +
            ", userId=" + userId +
            ", newNric='" + newNric + '\'' +
            ", occurredOn=" + occurredOn +
            '}';
    }
}
