package io.jeecloud.aidigitalsignage.agent.domain.event;

import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when an Agent is deleted.
 * 
 * This event signifies the removal of an agent from the system.
 * Consumers can use this event to:
 * - Clean up related data in other services
 * - Archive agent information
 * - Remove from search indexes
 * - Invalidate caches
 * - Trigger compliance/audit workflows
 */
public class AgentDeletedEvent implements DomainEvent {
    private final UUID eventId;
    private final String agentId;
    private final String agentCode;
    private final Instant occurredOn;
    private final String deletedBy; // User or system that performed deletion

    public AgentDeletedEvent(
        String agentId,
        String agentCode,
        String deletedBy
    ) {
        this.eventId = UUID.randomUUID();
        this.agentId = agentId;
        this.agentCode = agentCode;
        this.occurredOn = Instant.now();
        this.deletedBy = deletedBy != null ? deletedBy : "SYSTEM";
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
        return "AgentDeleted";
    }

    public String getAgentId() {
        return agentId;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    @Override
    public String toString() {
        return "AgentDeletedEvent{" +
                "eventId=" + eventId +
                ", agentId=" + agentId +
                ", agentCode='" + agentCode + '\'' +
                ", occurredOn=" + occurredOn +
                ", deletedBy='" + deletedBy + '\'' +
                '}';
    }
}
