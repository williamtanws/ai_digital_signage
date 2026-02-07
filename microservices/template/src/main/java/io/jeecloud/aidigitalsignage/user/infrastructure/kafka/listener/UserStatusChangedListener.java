package io.jeecloud.aidigitalsignage.user.infrastructure.kafka.listener;

import io.jeecloud.aidigitalsignage.user.domain.User;
import io.jeecloud.aidigitalsignage.user.infrastructure.kafka.producer.UserActiveStatusProducer;
import io.jeecloud.aidigitalsignage.user.infrastructure.persistence.UserAgentEntityManagerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Domain Event Listener for User status changes.
 * Publishes Kafka events after successful transaction.
 * Part of User component.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusChangedListener {

    private final UserActiveStatusProducer userActiveStatusProducer;
    private final UserAgentEntityManagerRepository userAgentRepository;

    /**
     * Listen to user activate/deactivate events and publish to Kafka.
     * This is triggered after the transaction commits successfully.
     * Publishes one event per agent associated with the user.
     *
     * @param user The user entity with status changed
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserStatusChanged(User user) {
        try {
            String userId = user.getId().getValue().toString();
            boolean status = user.isStatus();
            
            log.info("Publishing user status change event. UserId: {}, Status: {}",
                    userId, status);
            
            // Find all agents associated with this user
            List<String> agentCodes = userAgentRepository.findAgentCodesByUserId(userId);
            
            if (agentCodes.isEmpty()) {
                log.info("No agents associated with user {}. Skipping Kafka publish.", userId);
                return;
            }
            
            // Publish one event per agent
            for (String agentCode : agentCodes) {
                userActiveStatusProducer.publishUserActiveStatusChanged(
                        userId,
                        agentCode,
                        status
                );
            }
            
            log.info("Published {} user status change events for user {}", agentCodes.size(), userId);
        } catch (Exception e) {
            log.error("Failed to publish user status change event. UserId: {}, Error: {}",
                    user.getId().getValue(), e.getMessage(), e);
        }
    }
}
