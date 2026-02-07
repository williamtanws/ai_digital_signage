package io.jeecloud.aidigitalsignage.infrastructure.messaging;

import io.jeecloud.aidigitalsignage.common.application.port.out.EventPublisherPort;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import io.jeecloud.aidigitalsignage.common.infrastructure.messaging.KafkaEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Application-specific event publisher that routes events to correct topics.
 * 
 * This adapter wraps the generic KafkaEventPublisher from common
 * and adds application-specific routing logic via EventTopicResolver.
 * 
 * Pattern: Decorator/Wrapper Pattern
 * Location: Application Infrastructure Layer (template)
 * 
 * Architecture Notes:
 * - common provides generic KafkaEventPublisher (context-agnostic)
 * - template provides ApplicationEventPublisher (context-aware routing)
 * - This maintains clean separation between Shared Kernel and Application
 */
@Component
@Primary  // This will be injected instead of generic KafkaEventPublisher
public class ApplicationEventPublisher implements EventPublisherPort {
    
    private final KafkaEventPublisher kafkaEventPublisher;
    private final EventTopicResolver topicResolver;
    
    public ApplicationEventPublisher(
            KafkaEventPublisher kafkaEventPublisher,
            EventTopicResolver topicResolver) {
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.topicResolver = topicResolver;
    }
    
    @Override
    public void publish(DomainEvent event) {
        // Resolve the correct topic for this event
        String topic = topicResolver.resolveTopicForEvent(event);
        
        // Publish to the resolved topic
        kafkaEventPublisher.publish(topic, event);
    }
    
    @Override
    public void publish(String topic, DomainEvent event) {
        // Direct topic publication
        kafkaEventPublisher.publish(topic, event);
    }
}
