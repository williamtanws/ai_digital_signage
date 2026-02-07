package io.jeecloud.aidigitalsignage.agent.application.service;

import io.jeecloud.aidigitalsignage.agent.application.port.in.CreateAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.application.port.in.CreateAgentUseCase.CreateAgentCommand;
import io.jeecloud.aidigitalsignage.agent.application.port.in.DeleteAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.application.port.in.UpdateAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.application.port.in.UpdateAgentUseCase.UpdateAgentCommand;
import io.jeecloud.aidigitalsignage.agent.application.port.out.AgentCachePort;
import io.jeecloud.aidigitalsignage.agent.application.port.out.AgentEventPublisher;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.agent.domain.AgentRepository;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import io.jeecloud.aidigitalsignage.agent.domain.exception.AgentNotFoundException;
import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application Service for Agent Command operations (CQRS - Command side).
 * Orchestrates use cases and coordinates between domain and infrastructure.
 */
@Service
@Transactional
public class AgentCommandService implements 
    CreateAgentUseCase, 
    UpdateAgentUseCase, 
    DeleteAgentUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentCommandService.class);
    
    private final AgentRepository agentRepository;
    private final AgentEventPublisher eventPublisher;
    private final AgentCachePort cachePort;

    public AgentCommandService(
        AgentRepository agentRepository,
        AgentEventPublisher eventPublisher,
        AgentCachePort cachePort
    ) {
        this.agentRepository = agentRepository;
        this.eventPublisher = eventPublisher;
        this.cachePort = cachePort;
    }

    @Override
    public Agent createAgent(CreateAgentCommand command) {
        logger.info("Creating agent with code: {}", command.agentCode());
        
        // Business rule: Agent code must be unique
        if (agentRepository.existsByAgentCode(command.toAgentCode())) {
            throw new DomainException("Agent with code " + command.agentCode() + " already exists");
        }
        
        // Create domain entity
        Agent agent = Agent.create(
            command.toAgentCode(),
            command.name(),
            command.branchCode(),
            command.channel()
        );
        
        // Persist
        Agent savedAgent = agentRepository.save(agent);
        
        // Publish domain events
        publishDomainEvents(savedAgent);
        
        logger.info("Agent created successfully with ID: {}", savedAgent.getId());
        return savedAgent;
    }

    @Override
    public Agent updateAgent(UpdateAgentCommand command) {
        logger.info("Updating agent with code: {}", command.agentCode());
        
        // Load agent
        AgentCode agentCode = AgentCode.of(command.agentCode());
        Agent agent = agentRepository.findById(agentCode)
            .orElseThrow(() -> new AgentNotFoundException(agentCode));
        
        // Update domain entity
        agent.update(command.name(), command.branchCode(), command.channel());
        
        // Persist
        Agent updatedAgent = agentRepository.save(agent);
        
        // Publish domain events
        publishDomainEvents(updatedAgent);
        
        // Invalidate cache (let query service rebuild it)
        cachePort.invalidateAgent(agentCode);
        
        logger.info("Agent updated successfully: {}", command.agentCode());
        return updatedAgent;
    }
    
    @Override
    public Agent changeActiveStatus(ChangeActiveStatusCommand command) {
        logger.info("Changing active status for agent: {} to {}", command.agentCode(), command.status());
        
        // Load agent
        AgentCode agentCode = AgentCode.of(command.agentCode());
        Agent agent = agentRepository.findById(agentCode)
            .orElseThrow(() -> new AgentNotFoundException(agentCode));
        
        // Change active status using domain methods
        if (command.status()) {
            agent.activate();
        } else {
            agent.deactivate();
        }
        
        // Persist
        Agent updatedAgent = agentRepository.save(agent);
        
        // Publish domain events
        publishDomainEvents(updatedAgent);
        
        // Cache the updated agent
        cachePort.cacheAgent(agentCode, updatedAgent, 3600);
        
        logger.info("Agent active status changed successfully: {}", command.agentCode());
        return updatedAgent;
    }

    @Override
    public void deleteAgent(UUID agentId) {
        // For compatibility, keep UUID parameter but log warning
        throw new UnsupportedOperationException("Please use deleteAgentByCode instead");
    }
    
    public void deleteAgentByCode(String agentCode) {
        logger.info("Deleting agent with code: {}", agentCode);
        
        // Verify agent exists
        AgentCode code = AgentCode.of(agentCode);
        Agent agent = agentRepository.findById(code)
            .orElseThrow(() -> new AgentNotFoundException(code));
        
        // Delete
        agentRepository.deleteById(code);
        
        // Publish domain events (if agent has delete event logic)
        publishDomainEvents(agent);
        
        // Invalidate cache
        cachePort.invalidateAgent(code);
        
        logger.info("Agent deleted successfully: {}", agentCode);
    }

    /**
     * Publishes all domain events from the aggregate root.
     * Uses component-specific AgentEventPublisher for type-safe event handling.
     * Note: AgentStatusChangedEvent is handled by AgentRepositoryAdapter.save() 
     * which publishes the Agent entity to Spring's ApplicationEventPublisher.
     */
    private void publishDomainEvents(Agent agent) {
        for (DomainEvent event : agent.getDomainEvents()) {
            try {
                // Route to specific publisher method based on event type
                if (event instanceof io.jeecloud.aidigitalsignage.agent.domain.event.AgentCreatedEvent createdEvent) {
                    eventPublisher.publishAgentCreated(createdEvent);
                    logger.debug("Published AgentCreatedEvent: {}", event.getEventType());
                } else if (event instanceof io.jeecloud.aidigitalsignage.agent.domain.event.AgentUpdatedEvent updatedEvent) {
                    eventPublisher.publishAgentUpdated(updatedEvent);
                    logger.debug("Published AgentUpdatedEvent: {}", event.getEventType());
                } else if (event instanceof io.jeecloud.aidigitalsignage.agent.domain.event.AgentDeletedEvent deletedEvent) {
                    eventPublisher.publishAgentDeleted(deletedEvent);
                    logger.debug("Published AgentDeletedEvent: {}", event.getEventType());
                } else if (event instanceof io.jeecloud.aidigitalsignage.agent.domain.event.AgentStatusChangedEvent) {
                    // AgentStatusChangedEvent is no longer published to Kafka
                    // One-way sync only: User → Agent (not bidirectional)
                    logger.debug("AgentStatusChangedEvent (no external publishing needed): {}", event.getEventType());
                } else {
                    logger.warn("Unhandled event type: {}", event.getEventType());
                }
            } catch (Exception e) {
                logger.error("Failed to publish event: {}", event.getEventType(), e);
                // In production: implement retry logic or dead letter queue
                throw new RuntimeException("Failed to publish domain event", e);
            }
        }
        // Clear events after publishing
        agent.clearDomainEvents();
    }
}

