package io.jeecloud.aidigitalsignage.user.domain.event;

import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a new User is created.
 */
public class UserCreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final String userId;
    private final String newNric;
    private final String email;
    private final String name;
    private final Instant occurredOn;

    public UserCreatedEvent(
        String userId,
        String newNric,
        String email,
        String name,
        Instant occurredOn
    ) {
        super();
        this.eventId = UUID.randomUUID();
        this.userId = userId;
        this.newNric = newNric;
        this.email = email;
        this.name = name;
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
        return "UserCreated";
    }

    public String getUserId() {
        return userId;
    }

    public String getNewNric() {
        return newNric;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "UserCreatedEvent{" +
                "eventId=" + eventId +
                ", userId=" + userId +
                ", newNric='" + newNric + '\'' +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
