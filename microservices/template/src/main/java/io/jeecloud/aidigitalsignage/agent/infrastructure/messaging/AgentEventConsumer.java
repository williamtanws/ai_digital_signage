package io.jeecloud.aidigitalsignage.agent.infrastructure.messaging;

import io.jeecloud.aidigitalsignage.agent.domain.event.AgentCreatedEvent;
import io.jeecloud.aidigitalsignage.agent.domain.event.AgentDeletedEvent;
import io.jeecloud.aidigitalsignage.agent.domain.event.AgentUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer for Agent domain events.
 * 
 * This consumer listens to agent events published by this or other microservices
 * and can trigger side effects, analytics, or cross-component synchronization.
 * 
 * Pattern: Adapter (Hexagonal Architecture) - Primary/Driving Adapter
 * Location: Infrastructure Layer
 * Technology: Apache Kafka with Spring Kafka
 * 
 * Use Cases:
 * 1. Event-driven microservices communication
 * 2. Audit logging and analytics
 * 3. Cache invalidation in read replicas
 * 4. Cross-component data synchronization
 * 5. Triggering workflows in other bounded contexts
 * 
 * Event-Driven Architecture Benefits:
 * - Loose coupling between services
 * - Asynchronous processing
 * - Scalability (consumers can scale independently)
 * - Resilience (retry and dead letter queues)
 * 
 * Configuration:
 * - Topic: "agent-events" (defined in application.yml or KafkaConfig)
 * - Group ID: "sat-agent-consumer-group"
 * - Concurrency: Configurable for parallel processing
 * 
 * Example Scenarios:
 * 
 * Scenario 1: Analytics and Reporting
 * When an agent is created, send data to analytics service or data warehouse.
 * 
 * Scenario 2: Cache Synchronization
 * When an agent is updated in one instance, invalidate cache in all instances.
 * 
 * Scenario 3: Notification Service
 * When an agent is created, send welcome email or notification.
 * 
 * Scenario 4: Cross-Component Synchronization
 * When an agent is deleted, update related policies, claims, or commissions.
 * 
 * @see AgentEventPublisherAdapter
 */
@Component
public class AgentEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(AgentEventConsumer.class);
    
    /**
     * Consume AgentCreatedEvent from Kafka.
     * 
     * This method is automatically invoked when a message arrives on the agent-events topic
     * with the AgentCreatedEvent type.
     * 
     * @param event The agent created event
     */
    @KafkaListener(
        topics = "${app.kafka.topics.agent-events:agent-events}",
        groupId = "${app.kafka.consumer.group-id:sat-agent-consumer-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAgentCreatedEvent(AgentCreatedEvent event) {
        log.info("Received AgentCreatedEvent for agent code: {}", event.getAgentCode());
        
        try {
            // Example side effects:
            
            // 1. Send to analytics/data warehouse
            sendToAnalytics(event);
            
            // 2. Trigger notifications
            sendWelcomeNotification(event);
            
            // 3. Update search index (Elasticsearch)
            updateSearchIndex(event);
            
            // 4. Log for audit trail
            logAuditEvent(event);
            
            log.debug("Successfully processed AgentCreatedEvent: {}", event);
            
        } catch (Exception e) {
            log.error("Failed to process AgentCreatedEvent for agent code: {}", 
                     event.getAgentCode(), e);
            // In production:
            // - Retry logic (Spring Kafka supports @RetryableTopic)
            // - Send to dead letter queue (DLQ)
            // - Alert monitoring system
            throw new RuntimeException("Failed to process agent created event", e);
        }
    }
    
    /**
     * Consume AgentUpdatedEvent from Kafka.
     * 
     * @param event The agent updated event
     */
    @KafkaListener(
        topics = "${app.kafka.topics.agent-events:agent-events}",
        groupId = "${app.kafka.consumer.group-id:sat-agent-consumer-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAgentUpdatedEvent(AgentUpdatedEvent event) {
        log.info("Received AgentUpdatedEvent for agent code: {}", event.getAgentCode());
        
        try {
            // Example side effects:
            
            // 1. Invalidate cache in all service instances
            invalidateCache(event);
            
            // 2. Update search index
            updateSearchIndex(event);
            
            // 3. Notify related services (e.g., policy service if agent details changed)
            notifyRelatedServices(event);
            
            // 4. Log for audit trail
            logAuditEvent(event);
            
            log.debug("Successfully processed AgentUpdatedEvent: {}", event);
            
        } catch (Exception e) {
            log.error("Failed to process AgentUpdatedEvent for agent code: {}", 
                     event.getAgentCode(), e);
            throw new RuntimeException("Failed to process agent updated event", e);
        }
    }
    
    /**
     * Consume AgentDeletedEvent from Kafka.
     * 
     * @param event The agent deleted event
     */
    @KafkaListener(
        topics = "${app.kafka.topics.agent-events:agent-events}",
        groupId = "${app.kafka.consumer.group-id:sat-agent-consumer-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAgentDeletedEvent(AgentDeletedEvent event) {
        log.info("Received AgentDeletedEvent for agent code: {}", event.getAgentCode());
        
        try {
            // Example side effects:
            
            // 1. Remove from search index
            removeFromSearchIndex(event);
            
            // 2. Invalidate all related cache
            invalidateAllRelatedCache(event);
            
            // 3. Archive agent data
            archiveAgentData(event);
            
            // 4. Notify related services for cleanup
            notifyServicesForCleanup(event);
            
            // 5. Log for audit trail
            logAuditEvent(event);
            
            log.debug("Successfully processed AgentDeletedEvent: {}", event);
            
        } catch (Exception e) {
            log.error("Failed to process AgentDeletedEvent for agent code: {}", 
                     event.getAgentCode(), e);
            throw new RuntimeException("Failed to process agent deleted event", e);
        }
    }
    
    // ==================== Private Helper Methods ====================
    // These are placeholders showing where business logic would go
    
    private void sendToAnalytics(AgentCreatedEvent event) {
        // TODO: Implement integration with analytics service
        log.debug("Would send to analytics: {}", event);
    }
    
    private void sendWelcomeNotification(AgentCreatedEvent event) {
        // TODO: Implement notification service integration
        log.debug("Would send welcome notification for agent: {}", event.getAgentCode());
    }
    
    private void updateSearchIndex(Object event) {
        // TODO: Implement Elasticsearch index update
        log.debug("Would update search index: {}", event);
    }
    
    private void removeFromSearchIndex(AgentDeletedEvent event) {
        // TODO: Implement Elasticsearch index removal
        log.debug("Would remove from search index: {}", event.getAgentCode());
    }
    
    private void logAuditEvent(Object event) {
        // TODO: Implement audit logging (e.g., to database or audit service)
        log.info("Audit log: {}", event);
    }
    
    private void invalidateCache(AgentUpdatedEvent event) {
        // TODO: Implement cache invalidation (broadcast to all instances)
        log.debug("Would invalidate cache for agent: {}", event.getAgentCode());
    }
    
    private void invalidateAllRelatedCache(AgentDeletedEvent event) {
        // TODO: Implement comprehensive cache invalidation
        log.debug("Would invalidate all cache for agent: {}", event.getAgentCode());
    }
    
    private void notifyRelatedServices(AgentUpdatedEvent event) {
        // TODO: Implement inter-service communication
        log.debug("Would notify related services about update: {}", event.getAgentCode());
    }
    
    private void notifyServicesForCleanup(AgentDeletedEvent event) {
        // TODO: Implement cleanup notification to related services
        log.debug("Would notify services for cleanup: {}", event.getAgentCode());
    }
    
    private void archiveAgentData(AgentDeletedEvent event) {
        // TODO: Implement data archival (e.g., to S3 or archive database)
        log.debug("Would archive agent data: {}", event.getAgentCode());
    }
}
