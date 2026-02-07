package io.jeecloud.aidigitalsignage.agent.infrastructure.messaging;

import io.jeecloud.aidigitalsignage.agent.application.port.out.AgentEventPublisher;
import io.jeecloud.aidigitalsignage.agent.domain.event.AgentCreatedEvent;
import io.jeecloud.aidigitalsignage.agent.domain.event.AgentDeletedEvent;
import io.jeecloud.aidigitalsignage.agent.domain.event.AgentUpdatedEvent;
import io.jeecloud.aidigitalsignage.common.application.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Kafka Adapter for publishing Agent domain events.
 * 
 * This adapter implements the AgentEventPublisher port by delegating to the
 * generic EventPublisherPort infrastructure (Kafka). It provides component-specific
 * event publishing with appropriate topic routing and logging.
 * 
 * Pattern: Adapter (Hexagonal Architecture)
 * Location: Infrastructure Layer - Secondary Adapter
 * Technology: Kafka (via EventPublisherPort)
 * 
 * Event Flow:
 * 1. Domain Layer: Agent aggregate produces domain events
 * 2. Application Layer: Service calls this adapter through AgentEventPublisher port
 * 3. Infrastructure Layer: This adapter publishes to Kafka
 * 4. External Systems: Other microservices consume events from Kafka topics
 * 
 * Kafka Topics (configured in KafkaConfig):
 * - agent-events (for all agent domain events)
 * OR
 * - agent.created
 * - agent.updated
 * - agent.deleted
 * 
 * Example Usage in AgentCommandService:
 * <pre>
 * {@literal @}Transactional
 * public AgentResponse create(CreateAgentCommand command) {
 *     // 1. Create domain object
 *     Agent agent = Agent.create(...);
 *     
 *     // 2. Save to database
 *     Agent saved = agentRepository.save(agent);
 *     
 *     // 3. Publish domain events
 *     saved.getDomainEvents().forEach(event -> {
 *         if (event instanceof AgentCreatedEvent) {
 *             agentEventPublisher.publishAgentCreated((AgentCreatedEvent) event);
 *         }
 *     });
 *     saved.clearDomainEvents();
 *     
 *     return AgentResponse.from(saved);
 * }
 * </pre>
 * 
 * @see AgentEventPublisher
 * @see io.jeecloud.aidigitalsignage.shared.application.port.out.EventPublisherPort
 */
@Component
public class AgentEventPublisherAdapter implements AgentEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(AgentEventPublisherAdapter.class);
    
    private final EventPublisherPort eventPublisherPort;
    
    public AgentEventPublisherAdapter(EventPublisherPort eventPublisherPort) {
        this.eventPublisherPort = eventPublisherPort;
    }
    
    @Override
    public void publishAgentCreated(AgentCreatedEvent event) {
        log.info("Publishing AgentCreatedEvent for agent code: {}", event.getAgentCode());
        
        try {
            // Delegate to generic event publisher (Kafka)
            eventPublisherPort.publish(event);
            
            log.debug("Successfully published AgentCreatedEvent: {}", event);
        } catch (Exception e) {
            log.error("Failed to publish AgentCreatedEvent for agent code: {}", 
                     event.getAgentCode(), e);
            // In production, consider:
            // - Dead letter queue
            // - Event outbox pattern for guaranteed delivery
            // - Retry mechanism
            throw new RuntimeException("Failed to publish agent created event", e);
        }
    }
    
    @Override
    public void publishAgentUpdated(AgentUpdatedEvent event) {
        log.info("Publishing AgentUpdatedEvent for agent code: {}", event.getAgentCode());
        
        try {
            eventPublisherPort.publish(event);
            log.debug("Successfully published AgentUpdatedEvent: {}", event);
        } catch (Exception e) {
            log.error("Failed to publish AgentUpdatedEvent for agent code: {}", 
                     event.getAgentCode(), e);
            throw new RuntimeException("Failed to publish agent updated event", e);
        }
    }
    
    @Override
    public void publishAgentDeleted(AgentDeletedEvent event) {
        log.info("Publishing AgentDeletedEvent for agent ID: {}", event.getAgentId());
        
        try {
            eventPublisherPort.publish(event);
            log.debug("Successfully published AgentDeletedEvent: {}", event);
        } catch (Exception e) {
            log.error("Failed to publish AgentDeletedEvent for agent ID: {}", 
                     event.getAgentId(), e);
            throw new RuntimeException("Failed to publish agent deleted event", e);
        }
    }
}
