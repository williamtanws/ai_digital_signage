package io.jeecloud.aidigitalsignage.user.infrastructure.kafka.producer;

import io.jeecloud.aidigitalsignage.common.event.UserStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer for User Active Status events.
 * Part of User component - publishes events when user active status changes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActiveStatusProducer {

    private final KafkaTemplate<String, UserStatusChangedEvent> kafkaTemplate;

    @Value("${sat.kafka.topics.user-active-status:user-active-status}")
    private String topic;

    /**
     * Publish user status change event.
     *
     * @param userId The user ID
     * @param agentCode The agent code associated with the user
     * @param status The new status
     */
    public void publishUserActiveStatusChanged(String userId, String agentCode, boolean status) {
        UserStatusChangedEvent event = new UserStatusChangedEvent(
                userId,
                agentCode,
                status,
                Instant.now(),
                "USER_STATUS_CHANGED"
        );

        CompletableFuture<SendResult<String, UserStatusChangedEvent>> future =
                kafkaTemplate.send(topic, userId, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("User status event published successfully. UserId: {}, AgentCode: {}, Status: {}, Offset: {}",
                        userId, agentCode, status, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish user status event. UserId: {}, AgentCode: {}, Status: {}",
                        userId, agentCode, status, ex);
            }
        });
    }
}
