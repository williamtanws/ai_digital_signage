package io.jeecloud.aidigitalsignage.agent.domain.event;

import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event when Agent status changes.
 * This event will be published to Spring's event system and then to Kafka.
 */
@Getter
public class AgentStatusChangedEvent implements DomainEvent {
    private final UUID eventId;
    private final Agent agent;
    private final Instant occurredOn;

    public AgentStatusChangedEvent(Agent agent) {
        this.eventId = UUID.randomUUID();
        this.agent = agent;
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
        return "AGENT_STATUS_CHANGED";
    }
}
