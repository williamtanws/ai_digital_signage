package io.jeecloud.aidigitalsignage.user.infrastructure.messaging;

import io.jeecloud.aidigitalsignage.common.application.port.out.EventPublisherPort;
import io.jeecloud.aidigitalsignage.user.application.port.out.UserEventPublisher;
import io.jeecloud.aidigitalsignage.user.domain.event.UserCreatedEvent;
import io.jeecloud.aidigitalsignage.user.domain.event.UserDeletedEvent;
import io.jeecloud.aidigitalsignage.user.domain.event.UserUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Kafka Adapter for publishing User domain events.
 * 
 * This adapter implements the UserEventPublisher port by delegating to the
 * generic EventPublisherPort infrastructure (Kafka). It provides component-specific
 * event publishing with appropriate topic routing and logging.
 * 
 * Pattern: Adapter (Hexagonal Architecture)
 * Location: Infrastructure Layer - Secondary Adapter
 * Technology: Kafka (via EventPublisherPort)
 */
@Component
public class UserEventPublisherAdapter implements UserEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(UserEventPublisherAdapter.class);
    
    private final EventPublisherPort eventPublisherPort;
    
    public UserEventPublisherAdapter(EventPublisherPort eventPublisherPort) {
        this.eventPublisherPort = eventPublisherPort;
    }
    
    @Override
    public void publishUserCreated(UserCreatedEvent event) {
        log.info("Publishing UserCreatedEvent for user ID: {}", event.getUserId());
        
        try {
            // Delegate to generic event publisher (Kafka)
            eventPublisherPort.publish(event);
            
            log.debug("Successfully published UserCreatedEvent: {}", event);
        } catch (Exception e) {
            log.error("Failed to publish UserCreatedEvent for user ID: {}", 
                     event.getUserId(), e);
            throw new RuntimeException("Failed to publish user created event", e);
        }
    }
    
    @Override
    public void publishUserUpdated(UserUpdatedEvent event) {
        log.info("Publishing UserUpdatedEvent for user ID: {}", event.getUserId());
        
        try {
            eventPublisherPort.publish(event);
            log.debug("Successfully published UserUpdatedEvent: {}", event);
        } catch (Exception e) {
            log.error("Failed to publish UserUpdatedEvent for user ID: {}", 
                     event.getUserId(), e);
            throw new RuntimeException("Failed to publish user updated event", e);
        }
    }
    
    @Override
    public void publishUserDeleted(UserDeletedEvent event) {
        log.info("Publishing UserDeletedEvent for user ID: {}", event.getUserId());
        
        try {
            eventPublisherPort.publish(event);
            log.debug("Successfully published UserDeletedEvent: {}", event);
        } catch (Exception e) {
            log.error("Failed to publish UserDeletedEvent for user ID: {}", 
                     event.getUserId(), e);
            throw new RuntimeException("Failed to publish user deleted event", e);
        }
    }
}
