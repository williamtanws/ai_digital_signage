package io.jeecloud.aidigitalsignage.user.domain.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserUpdatedEventTest {

    @Test
    void shouldCreateUserUpdatedEventWithAllFields() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        String newNric = "950101-01-1234";
        String email = "updated@example.com";
        String name = "Updated User";
        Instant occurredOn = Instant.now();

        // When
        UserUpdatedEvent event = new UserUpdatedEvent(userId, newNric, email, name, occurredOn);

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
        UserUpdatedEvent event1 = new UserUpdatedEvent(userId, "NRIC1", "email1@test.com", "Name 1", now);
        UserUpdatedEvent event2 = new UserUpdatedEvent(userId, "NRIC2", "email2@test.com", "Name 2", now);

        // Then
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void shouldReturnCorrectEventType() {
        // Given
        UserUpdatedEvent event = new UserUpdatedEvent(
            "test-user-" + System.currentTimeMillis(),
            "NRIC",
            "email@test.com",
            "Full Name",
            Instant.now()
        );

        // When
        String eventType = event.getEventType();

        // Then
        assertThat(eventType).isEqualTo("UserUpdated");
    }

    @Test
    void shouldImplementDomainEventInterface() {
        // Given
        UserUpdatedEvent event = new UserUpdatedEvent(
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

        UserUpdatedEvent event = new UserUpdatedEvent(userId, nric, "email@test.com", "Updated User", occurredOn);

        // When
        String toString = event.toString();

        // Then
        assertThat(toString).contains("UserUpdatedEvent");
        assertThat(toString).contains(userId.toString());
        assertThat(toString).contains(nric);
    }

    @Test
    void shouldHandleNullFields() {
        // When
        UserUpdatedEvent event = new UserUpdatedEvent(
            "test-user-" + System.currentTimeMillis(),
            null,
            null,
            null,
            Instant.now()
        );

        // Then
        assertThat(event.getNewNric()).isNull();
        assertThat(event.getEmail()).isNull();
        assertThat(event.getName()).isNull();
    }

    @Test
    void shouldAllowSameUserIdForMultipleUpdates() {
        // Given
        String sameUserId = "test-user-" + System.currentTimeMillis();

        // When
        UserUpdatedEvent event1 = new UserUpdatedEvent(sameUserId, "NRIC1", "email@test.com", "Name", Instant.now());
        UserUpdatedEvent event2 = new UserUpdatedEvent(sameUserId, "NRIC2", "email2@test.com", "Name 2", Instant.now());

        // Then
        assertThat(event1.getUserId()).isEqualTo(event2.getUserId());
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }
}
