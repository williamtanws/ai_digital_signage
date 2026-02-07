package io.jeecloud.aidigitalsignage.infrastructure.messaging;

import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Resolves the correct Kafka topic for domain events based on event type.
 * 
 * This component provides topic routing logic to ensure different domain events
 * are published to their respective topics.
 * 
 * Pattern: Strategy Pattern for topic resolution
 * Location: Application Infrastructure Layer (template)
 * 
 * NOTE: This is application-specific logic and belongs in template,
 * not in common (Shared Kernel). common should remain context-agnostic.
 */
@Component
public class EventTopicResolver {
    
    @Value("${sat.agent.event.topic-name:agent-events}")
    private String agentEventTopic;
    
    @Value("${sat.user.event.topic-name:user-events}")
    private String userEventTopic;
    
    /**
     * Resolve the Kafka topic name based on the event class name.
     * 
     * @param event The domain event
     * @return The topic name for the event
     */
    public String resolveTopicForEvent(DomainEvent event) {
        String eventClassName = event.getClass().getName();
        
        // Route to user-events topic
        if (eventClassName.contains(".user.domain.event.")) {
            return userEventTopic;
        }
        
        // Route to agent-events topic (default)
        return agentEventTopic;
    }
    
    /**
     * Get the configured agent events topic name.
     */
    public String getAgentEventTopic() {
        return agentEventTopic;
    }
    
    /**
     * Get the configured user events topic name.
     */
    public String getUserEventTopic() {
        return userEventTopic;
    }
}
