package io.jeecloud.aidigitalsignage.agent.domain.event;

import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when an Agent is updated.
 */
public class AgentUpdatedEvent implements DomainEvent {
    private final UUID eventId;
    private final String agentCode;
    private final String name;
    private final String branchCode;
    private final Channel channel;
    private final Instant occurredOn;

    public AgentUpdatedEvent(
        String agentCode,
        String name,
        String branchCode,
        Channel channel,
        Instant occurredOn
    ) {
        this.eventId = UUID.randomUUID();
        this.agentCode = agentCode;
        this.name = name;
        this.branchCode = branchCode;
        this.channel = channel;
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
        return "AgentUpdated";
    }

    public String getAgentCode() {
        return agentCode;
    }

    public String getName() {
        return name;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "AgentUpdatedEvent{" +
                "eventId=" + eventId +
                ", agentCode='" + agentCode + '\'' +
                ", occurredOn=" + occurredOn +
                '}';
    }
}

