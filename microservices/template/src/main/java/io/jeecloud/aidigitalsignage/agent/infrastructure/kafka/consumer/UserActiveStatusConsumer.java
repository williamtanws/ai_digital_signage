package io.jeecloud.aidigitalsignage.agent.infrastructure.kafka.consumer;

import io.jeecloud.aidigitalsignage.agent.application.port.out.AgentCachePort;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.agent.domain.AgentRepository;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.common.event.UserStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Kafka Consumer for User Active Status events in Agent Component.
 * Implements ONE-WAY sync: User → Agent
 * 
 * When a user is deactivated, the specified agent will be deactivated.
 * When a user is activated, the specified agent will be activated.
 * Cache is invalidated for the updated agent to ensure consistency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActiveStatusConsumer {

    private final AgentRepository agentRepository;
    private final AgentCachePort agentCachePort;

    /**
     * Consume user active status change event and update all associated agents.
     * 
     * @param event The user active status changed event
     * @param acknowledgment Manual acknowledgment
     */
    @Transactional
    @KafkaListener(
            topics = "${sat.kafka.topics.user-active-status:user-active-status}",
            groupId = "sat-agent-service-dev",
            containerFactory = "userEventKafkaListenerContainerFactory"
    )
    public void consumeUserActiveStatusChanged(
            UserStatusChangedEvent event,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received user status change event. UserId: {}, AgentCode: {}, Status: {}",
                    event.getUserId(), event.getAgentCode(), event.isStatus());

            // Validate agentCode is provided
            if (event.getAgentCode() == null || event.getAgentCode().isEmpty()) {
                log.warn("AgentCode not provided in event. UserId: {}. Skipping update.", event.getUserId());
                acknowledgment.acknowledge();
                return;
            }
            
            // Find the specific agent
            AgentCode agentCode = AgentCode.of(event.getAgentCode());
            Optional<Agent> agentOpt = agentRepository.findByAgentCode(agentCode);
            
            if (agentOpt.isEmpty()) {
                log.warn("Agent {} not found in repository. UserId: {}. Skipping update.", 
                        event.getAgentCode(), event.getUserId());
                acknowledgment.acknowledge();
                return;
            }
            
            Agent agent = agentOpt.get();
            boolean previousStatus = agent.isStatus();
            
            if (event.isStatus()) {
                agent.activate();
                log.debug("Activated agent {} due to user {} activation", 
                        agent.getAgentCode().value(), event.getUserId());
            } else {
                agent.deactivate();
                log.debug("Deactivated agent {} due to user {} deactivation", 
                        agent.getAgentCode().value(), event.getUserId());
            }
            
            // Only save if status actually changed
            if (previousStatus != agent.isStatus()) {
                agentRepository.save(agent);
                
                // Invalidate cache for the updated agent
                agentCachePort.invalidateAgent(agent.getAgentCode());
                
                log.info("Updated agent {} status to {} due to user {} status change",
                        agent.getAgentCode().value(), agent.isStatus(), event.getUserId());
            } else {
                log.debug("Agent {} status unchanged (already {}). No update needed.",
                        agent.getAgentCode().value(), agent.isStatus());
            }

            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();
            log.info("Successfully processed user active status change event. UserId: {}, AgentCode: {}", 
                    event.getUserId(), event.getAgentCode());

        } catch (Exception e) {
            log.error("Error processing user active status change event. UserId: {}, AgentCode: {}, Error: {}",
                    event.getUserId(), event.getAgentCode(), e.getMessage(), e);
            // Don't acknowledge on error - message will be reprocessed
            throw new RuntimeException("Failed to process user active status change event", e);
        }
    }
}
