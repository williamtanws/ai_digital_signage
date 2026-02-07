package io.jeecloud.aidigitalsignage.common.infrastructure.messaging;

import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SendResult<String, String> sendResult;

    private KafkaEventPublisher publisher;

    // Test event implementation
    private record TestEvent(
        UUID eventId,
        String eventType,
        Instant occurredOn,
        String data
    ) implements DomainEvent {
        @Override
        public UUID getEventId() { return eventId; }

        @Override
        public String getEventType() { return eventType; }

        @Override
        public Instant getOccurredOn() { return occurredOn; }
    }

    @BeforeEach
    void setUp() {
        publisher = new KafkaEventPublisher(kafkaTemplate, objectMapper);
    }

    @Test
    void shouldPublishEventToDefaultTopic() throws JsonProcessingException {
        // Given
        TestEvent event = new TestEvent(
            UUID.randomUUID(),
            "TestEvent",
            Instant.now(),
            "test-data"
        );
        String eventJson = "{\"eventId\":\"123\",\"eventType\":\"TestEvent\"}";
        
        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(isNull(), eq(event.eventId().toString()), eq(eventJson))).thenReturn(future);

        // When
        publisher.publish(event);

        // Then
        verify(kafkaTemplate).send(isNull(), eq(event.eventId().toString()), eq(eventJson));
        verify(objectMapper).writeValueAsString(event);
    }

    @Test
    void shouldPublishEventToSpecifiedTopic() throws JsonProcessingException {
        // Given
        TestEvent event = new TestEvent(
            UUID.randomUUID(),
            "TestEvent",
            Instant.now(),
            "test-data"
        );
        String customTopic = "custom-topic";
        String eventJson = "{\"eventId\":\"123\"}";
        
        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // When
        publisher.publish(customTopic, event);

        // Then
        verify(kafkaTemplate).send(eq(customTopic), eq(event.eventId().toString()), eq(eventJson));
    }

    @Test
    void shouldUseEventIdAsMessageKey() throws JsonProcessingException {
        // Given
        UUID eventId = UUID.randomUUID();
        TestEvent event = new TestEvent(eventId, "TestEvent", Instant.now(), "data");
        
        when(objectMapper.writeValueAsString(event)).thenReturn("{}");
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // When
        publisher.publish("topic", event);

        // Then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), anyString());
        assertThat(keyCaptor.getValue()).isEqualTo(eventId.toString());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenSerializationFails() throws JsonProcessingException {
        // Given
        TestEvent event = new TestEvent(UUID.randomUUID(), "TestEvent", Instant.now(), "data");
        JsonProcessingException exception = new JsonProcessingException("Serialization failed") {};
        
        when(objectMapper.writeValueAsString(event)).thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> publisher.publish("topic", event))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Event serialization failed")
            .hasCauseInstanceOf(JsonProcessingException.class);
        
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void shouldSerializeEventBeforeSending() throws JsonProcessingException {
        // Given
        TestEvent event = new TestEvent(UUID.randomUUID(), "TestEvent", Instant.now(), "data");
        String expectedJson = "{\"data\":\"test-data\"}";
        
        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // When
        publisher.publish("topic", event);

        // Then
        verify(objectMapper).writeValueAsString(event);
        verify(kafkaTemplate).send(anyString(), anyString(), eq(expectedJson));
    }

    @Test
    void shouldHandleMultipleEvents() throws JsonProcessingException {
        // Given
        TestEvent event1 = new TestEvent(UUID.randomUUID(), "Event1", Instant.now(), "data1");
        TestEvent event2 = new TestEvent(UUID.randomUUID(), "Event2", Instant.now(), "data2");
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{}", "{}");
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // When
        publisher.publish("topic", event1);
        publisher.publish("topic", event2);

        // Then
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), anyString());
        verify(objectMapper, times(2)).writeValueAsString(any());
    }
}
