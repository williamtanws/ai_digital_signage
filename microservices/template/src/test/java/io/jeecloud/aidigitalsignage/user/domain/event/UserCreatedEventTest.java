package io.jeecloud.aidigitalsignage.user.domain.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreatedEventTest {

    @Test
    void shouldCreateUserCreatedEventWithAllFields() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        String newNric = "950101-01-1234";
        String email = "test@example.com";
        String name = "Test User";
        Instant occurredOn = Instant.now();

        // When
        UserCreatedEvent event = new UserCreatedEvent(userId, newNric, email, name, occurredOn);

        // Then
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getNewNric()).isEqualTo(newNric);
        assertThat(event.getEmail()).isEqualTo(email);
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getOccurredOn()).isNotNull();
    }

    @Test
    void shouldGenerateUniqueEventId() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        Instant now = Instant.now();

        // When
        UserCreatedEvent event1 = new UserCreatedEvent(userId, "NRIC1", "user1@test.com", "User One", now);
        UserCreatedEvent event2 = new UserCreatedEvent(userId, "NRIC2", "user2@test.com", "User Two", now);

        // Then
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void shouldReturnCorrectEventType() {
        // Given
        UserCreatedEvent event = new UserCreatedEvent(
            "test-user-" + System.currentTimeMillis(),
            "NRIC",
            "email@test.com",
            "Full Name",
            Instant.now()
        );

        // When
        String eventType = event.getEventType();

        // Then
        assertThat(eventType).isEqualTo("UserCreated");
    }

    @Test
    void shouldImplementDomainEventInterface() {
        // Given
        UserCreatedEvent event = new UserCreatedEvent(
            "test-user-" + System.currentTimeMillis(),
            "NRIC",
            "email@test.com",
            "Full Name",
            Instant.now()
        );

        // Then
        assertThat(event).isInstanceOf(io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent.class);
    }

    @Test
    void shouldProvideToStringRepresentation() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        String nric = "950101-01-1234";
        Instant occurredOn = Instant.now();

        UserCreatedEvent event = new UserCreatedEvent(userId, nric, "email@test.com", "Test User", occurredOn);

        // When
        String toString = event.toString();

        // Then
        assertThat(toString).contains("UserCreatedEvent");
        assertThat(toString).contains(userId.toString());
        assertThat(toString).contains(nric);
    }

    @Test
    void shouldHandleNullEmail() {
        // When
        UserCreatedEvent event = new UserCreatedEvent(
            "test-user-" + System.currentTimeMillis(),
            "NRIC",
            null,
            "Full Name",
            Instant.now()
        );

        // Then
        assertThat(event.getEmail()).isNull();
    }

    @Test
    void shouldHandleNullFullName() {
        // When
        UserCreatedEvent event = new UserCreatedEvent(
            "test-user-" + System.currentTimeMillis(),
            "NRIC",
            "email@test.com",
            null,
            Instant.now()
        );

        // Then
        assertThat(event.getName()).isNull();
    }
}
