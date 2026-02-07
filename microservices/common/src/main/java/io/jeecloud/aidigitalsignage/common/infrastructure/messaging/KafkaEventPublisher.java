package io.jeecloud.aidigitalsignage.common.infrastructure.messaging;

import io.jeecloud.aidigitalsignage.common.application.port.out.EventPublisherPort;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter for publishing domain events.
 * Secondary/Driven Adapter in Hexagonal Architecture.
 * 
 * NOTE: This is a generic adapter in common (Shared Kernel).
 * It publishes to a default topic. For domain-specific routing,
 * use application-specific EventPublisher wrappers in template.
 */
@Component
public class KafkaEventPublisher implements EventPublisherPort {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${sat.event.default-topic:common-events}")
    private String defaultTopic;

    public KafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate, 
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        // Publish to default topic
        publish(defaultTopic, event);
    }

    @Override
    public void publish(String topic, DomainEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.getEventId().toString();
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(topic, key, eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.error("Failed to publish event {} to topic {}", 
                        event.getEventType(), topic, ex);
                } else {
                    logger.info("Successfully published event {} to topic {} with offset {}", 
                        event.getEventType(), topic, result.getRecordMetadata().offset());
                }
            });
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event: {}", event.getEventType(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }
}

