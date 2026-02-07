package io.jeecloud.aidigitalsignage.agent.infrastructure.kafka.config;

import org.springframework.context.annotation.Configuration;

/**
 * Kafka Configuration for Agent Component.
 * Agent component only consumes user-active-status events (one-way sync: User → Agent).
 * No producer configuration needed as Agent doesn't publish status changes to other components.
 */
@Configuration
public class AgentKafkaConfig {
    // Configuration handled by UserKafkaConfig for consuming user-active-status events
}
