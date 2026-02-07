package io.jeecloud.aidigitalsignage.agent.infrastructure.messaging;

import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import io.jeecloud.aidigitalsignage.agent.domain.event.AgentCreatedEvent;
import io.jeecloud.aidigitalsignage.agent.domain.event.AgentDeletedEvent;
import io.jeecloud.aidigitalsignage.agent.domain.event.AgentUpdatedEvent;
import io.jeecloud.aidigitalsignage.common.application.port.out.EventPublisherPort;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AgentEventPublisherAdapter.
 * Tests Kafka event publishing with proper error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgentEventPublisherAdapter Tests")
class AgentEventPublisherAdapterTest {

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private AgentEventPublisherAdapter agentEventPublisherAdapter;

    private AgentCreatedEvent agentCreatedEvent;
    private AgentUpdatedEvent agentUpdatedEvent;
    private AgentDeletedEvent agentDeletedEvent;

    @BeforeEach
    void setUp() {
        Instant timestamp = Instant.now();
        
        agentCreatedEvent = new AgentCreatedEvent(
            "AG00001",
            "Test Agent",
            "BR0001",
            Channel.DIRECT,
            timestamp
        );
        
        agentUpdatedEvent = new AgentUpdatedEvent(
            "AG00001",
            "Updated Agent",
            "BR0002",
            Channel.BROKER,
            timestamp
        );
        
        agentDeletedEvent = new AgentDeletedEvent(
            "test-user-" + System.currentTimeMillis(),
            "AG00001",
            "SYSTEM"
        );
    }

    @Test
    @DisplayName("Should publish agent created event successfully")
    void shouldPublishAgentCreatedEventSuccessfully() {
        // Given
        doNothing().when(eventPublisherPort).publish(any(DomainEvent.class));

        // When
        agentEventPublisherAdapter.publishAgentCreated(agentCreatedEvent);

        // Then
        verify(eventPublisherPort).publish(agentCreatedEvent);
        verifyNoMoreInteractions(eventPublisherPort);
    }

    @Test
    @DisplayName("Should publish agent updated event successfully")
    void shouldPublishAgentUpdatedEventSuccessfully() {
        // Given
        doNothing().when(eventPublisherPort).publish(any(DomainEvent.class));

        // When
        agentEventPublisherAdapter.publishAgentUpdated(agentUpdatedEvent);

        // Then
        verify(eventPublisherPort).publish(agentUpdatedEvent);
        verifyNoMoreInteractions(eventPublisherPort);
    }

    @Test
    @DisplayName("Should publish agent deleted event successfully")
    void shouldPublishAgentDeletedEventSuccessfully() {
        // Given
        doNothing().when(eventPublisherPort).publish(any(DomainEvent.class));

        // When
        agentEventPublisherAdapter.publishAgentDeleted(agentDeletedEvent);

        // Then
        verify(eventPublisherPort).publish(agentDeletedEvent);
        verifyNoMoreInteractions(eventPublisherPort);
    }

    @Test
    @DisplayName("Should throw RuntimeException when publishing agent created event fails")
    void shouldThrowRuntimeExceptionWhenPublishingAgentCreatedEventFails() {
        // Given
        Exception originalException = new RuntimeException("Kafka connection failed");
        doThrow(originalException).when(eventPublisherPort).publish(any(DomainEvent.class));

        // When / Then
        assertThatThrownBy(() -> agentEventPublisherAdapter.publishAgentCreated(agentCreatedEvent))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to publish agent created event")
            .hasCause(originalException);

        verify(eventPublisherPort).publish(agentCreatedEvent);
    }

    @Test
    @DisplayName("Should throw RuntimeException when publishing agent updated event fails")
    void shouldThrowRuntimeExceptionWhenPublishingAgentUpdatedEventFails() {
        // Given
        Exception originalException = new RuntimeException("Kafka connection failed");
        doThrow(originalException).when(eventPublisherPort).publish(any(DomainEvent.class));

        // When / Then
        assertThatThrownBy(() -> agentEventPublisherAdapter.publishAgentUpdated(agentUpdatedEvent))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to publish agent updated event")
            .hasCause(originalException);

        verify(eventPublisherPort).publish(agentUpdatedEvent);
    }

    @Test
    @DisplayName("Should throw RuntimeException when publishing agent deleted event fails")
    void shouldThrowRuntimeExceptionWhenPublishingAgentDeletedEventFails() {
        // Given
        Exception originalException = new RuntimeException("Kafka connection failed");
        doThrow(originalException).when(eventPublisherPort).publish(any(DomainEvent.class));

        // When / Then
        assertThatThrownBy(() -> agentEventPublisherAdapter.publishAgentDeleted(agentDeletedEvent))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to publish agent deleted event")
            .hasCause(originalException);

        verify(eventPublisherPort).publish(agentDeletedEvent);
    }

    @Test
    @DisplayName("Should handle null pointer exception during event publishing")
    void shouldHandleNullPointerExceptionDuringEventPublishing() {
        // Given
        Exception originalException = new NullPointerException("Event publisher not initialized");
        doThrow(originalException).when(eventPublisherPort).publish(any(DomainEvent.class));

        // When / Then
        assertThatThrownBy(() -> agentEventPublisherAdapter.publishAgentCreated(agentCreatedEvent))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to publish agent created event");

        verify(eventPublisherPort).publish(agentCreatedEvent);
    }

    @Test
    @DisplayName("Should publish multiple events in sequence")
    void shouldPublishMultipleEventsInSequence() {
        // Given
        doNothing().when(eventPublisherPort).publish(any(DomainEvent.class));

        // When
        agentEventPublisherAdapter.publishAgentCreated(agentCreatedEvent);
        agentEventPublisherAdapter.publishAgentUpdated(agentUpdatedEvent);
        agentEventPublisherAdapter.publishAgentDeleted(agentDeletedEvent);

        // Then
        verify(eventPublisherPort, times(3)).publish(any(DomainEvent.class));
        verify(eventPublisherPort).publish(agentCreatedEvent);
        verify(eventPublisherPort).publish(agentUpdatedEvent);
        verify(eventPublisherPort).publish(agentDeletedEvent);
    }

    @Test
    @DisplayName("Should handle different event payloads correctly")
    void shouldHandleDifferentEventPayloadsCorrectly() {
        // Given
        Instant timestamp = Instant.now();
        AgentCreatedEvent event1 = new AgentCreatedEvent(
            "AG00001",
            "Agent One",
            "BR0001",
            Channel.DIRECT,
            timestamp
        );
        
        AgentCreatedEvent event2 = new AgentCreatedEvent(
            "AG00002",
            "Agent Two",
            "BR0002",
            Channel.BROKER,
            timestamp
        );

        doNothing().when(eventPublisherPort).publish(any(DomainEvent.class));

        // When
        agentEventPublisherAdapter.publishAgentCreated(event1);
        agentEventPublisherAdapter.publishAgentCreated(event2);

        // Then
        verify(eventPublisherPort, times(2)).publish(any(DomainEvent.class));
    }

    @Test
    @DisplayName("Should not swallow exceptions when publishing fails")
    void shouldNotSwallowExceptionsWhenPublishingFails() {
        // Given
        RuntimeException exception = new RuntimeException("Message broker unavailable");
        doThrow(exception).when(eventPublisherPort).publish(any(DomainEvent.class));

        // When / Then
        assertThatThrownBy(() -> agentEventPublisherAdapter.publishAgentCreated(agentCreatedEvent))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to publish agent created event");

        assertThatThrownBy(() -> agentEventPublisherAdapter.publishAgentUpdated(agentUpdatedEvent))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to publish agent updated event");

        assertThatThrownBy(() -> agentEventPublisherAdapter.publishAgentDeleted(agentDeletedEvent))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to publish agent deleted event");
    }

    @Test
    @DisplayName("Should publish event with all required fields")
    void shouldPublishEventWithAllRequiredFields() {
        // Given
        doNothing().when(eventPublisherPort).publish(any(DomainEvent.class));

        // When
        agentEventPublisherAdapter.publishAgentCreated(agentCreatedEvent);

        // Then
        assertThat(agentCreatedEvent.getAgentCode()).isEqualTo("AG00001");
        assertThat(agentCreatedEvent.getName()).isEqualTo("Test Agent");
        assertThat(agentCreatedEvent.getBranchCode()).isEqualTo("BR0001");
        assertThat(agentCreatedEvent.getChannel()).isEqualTo(Channel.DIRECT);
        assertThat(agentCreatedEvent.getEventId()).isNotNull();
        assertThat(agentCreatedEvent.getOccurredOn()).isNotNull();

        verify(eventPublisherPort).publish(agentCreatedEvent);
    }
}
