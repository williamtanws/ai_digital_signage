package io.jeecloud.aidigitalsignage.user.domain.event;

import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import io.jeecloud.aidigitalsignage.user.domain.User;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event when User status changes.
 * This event will be published to Spring's event system and then to Kafka.
 */
@Getter
public class UserStatusChangedEvent implements DomainEvent {
    private final UUID eventId;
    private final User user;
    private final Instant occurredOn;

    public UserStatusChangedEvent(User user) {
        this.eventId = UUID.randomUUID();
        this.user = user;
        this.occurredOn = Instant.now();
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
        return "USER_STATUS_CHANGED";
    }
}
