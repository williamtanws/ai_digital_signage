package io.jeecloud.aidigitalsignage.agent.domain;

import io.jeecloud.aidigitalsignage.agent.domain.event.AgentCreatedEvent;
import io.jeecloud.aidigitalsignage.agent.domain.event.AgentStatusChangedEvent;
import io.jeecloud.aidigitalsignage.agent.domain.event.AgentUpdatedEvent;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;
import io.jeecloud.aidigitalsignage.common.domain.base.AggregateRoot;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Agent - Aggregate Root in DDD.
 * Represents an insurance agent with business identity and behavior.
 * Maintains consistency boundaries and publishes domain events.
 * Uses AgentCode as the primary key.
 */
public class Agent implements AggregateRoot<AgentCode> {
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int BRANCH_CODE_LENGTH = 6;

    private final AgentCode agentCode;
    private String name;
    private String branchCode;
    private Channel channel;
    private boolean status;
    private Instant createDt;
    private Instant updateDt;
    
    // Domain events to be published
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Private constructor - use factory methods
    private Agent(AgentCode agentCode) {
        this.agentCode = Objects.requireNonNull(agentCode, "AgentCode cannot be null");
        this.status = true;
        this.createDt = Instant.now();
        this.updateDt = Instant.now();
    }

    /**
     * Factory method to create a new Agent.
     * Publishes AgentCreatedEvent.
     */
    public static Agent create(AgentCode agentCode, String name, String branchCode, Channel channel) {
        Agent agent = new Agent(agentCode);
        agent.setName(name);
        agent.setBranchCode(branchCode);
        agent.setChannel(channel);
        
        // Publish domain event
        agent.registerEvent(new AgentCreatedEvent(
            agent.getId().value(),
            agent.getName(),
            agent.getBranchCode(),
            agent.getChannel(),
            agent.getCreateDt()
        ));
        
        return agent;
    }

    /**
     * Factory method to reconstitute an Agent from persistence.
     */
    public static Agent reconstitute(
        AgentCode agentCode,
        String name,
        String branchCode,
        Channel channel,
        boolean status,
        Instant createDt,
        Instant updateDt
    ) {
        Agent agent = new Agent(agentCode);
        agent.name = name;
        agent.branchCode = branchCode;
        agent.channel = channel;
        agent.status = status;
        agent.createDt = createDt;
        agent.updateDt = updateDt;
        return agent;
    }

    /**
     * Business method to update agent information.
     * Publishes AgentUpdatedEvent.
     */
    public void update(String name, String branchCode, Channel channel) {
        setName(name);
        setBranchCode(branchCode);
        setChannel(channel);
        this.updateDt = Instant.now();
        
        // Publish domain event
        registerEvent(new AgentUpdatedEvent(
            this.agentCode.value(),
            this.name,
            this.branchCode,
            this.channel,
            this.updateDt
        ));
    }

    /**
     * Business method to deactivate an agent.
     */
    public void deactivate() {
        if (!this.status) {
            throw new DomainException("Agent is already inactive");
        }
        this.status = false;
        this.updateDt = Instant.now();
        
        // Publish domain event for status change
        registerEvent(new AgentStatusChangedEvent(this));
    }

    /**
     * Business method to activate an agent.
     */
    public void activate() {
        if (this.status) {
            throw new DomainException("Agent is already active");
        }
        this.status = true;
        this.updateDt = Instant.now();
        
        // Publish domain event for status change
        registerEvent(new AgentStatusChangedEvent(this));
    }

    /**
     * Business rule: Check if agent can process transactions.
     */
    public boolean canProcessTransactions() {
        return this.status && this.channel != null;
    }

    // Setters with validation
    private void setName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new DomainException("Agent name cannot be null or empty");
        }
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new DomainException(
                "Agent name must be between %d and %d characters".formatted(MIN_NAME_LENGTH, MAX_NAME_LENGTH)
            );
        }
        this.name = name.trim();
    }

    private void setBranchCode(String branchCode) {
        if (StringUtils.isBlank(branchCode)) {
            throw new DomainException("Branch code cannot be null or empty");
        }
        if (branchCode.length() != BRANCH_CODE_LENGTH) {
            throw new DomainException(
                "Branch code must be exactly %d characters".formatted(BRANCH_CODE_LENGTH)
            );
        }
        this.branchCode = branchCode.toUpperCase();
    }

    private void setChannel(Channel channel) {
        this.channel = Objects.requireNonNull(channel, "Channel cannot be null");
    }

    // Event handling
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    // Getters
    @Override
    public AgentCode getId() {
        return agentCode;
    }

    public AgentCode getAgentCode() {
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

    public boolean isStatus() {
        return status;
    }

    public Instant getCreateDt() {
        return createDt;
    }

    public Instant getUpdateDt() {
        return updateDt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return Objects.equals(agentCode, agent.agentCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentCode);
    }

    @Override
    public String toString() {
        return "Agent{" +
                "agentCode=" + agentCode +
                ", name='" + name + '\'' +
                ", branchCode='" + branchCode + '\'' +
                ", channel=" + channel +
                ", status=" + status +
                '}';
    }
}

