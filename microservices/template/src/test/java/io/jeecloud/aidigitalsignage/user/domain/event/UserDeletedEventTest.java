package io.jeecloud.aidigitalsignage.user.domain.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserDeletedEventTest {

    @Test
    void shouldCreateUserDeletedEventWithAllFields() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        String newNric = "950101-01-1234";
        Instant occurredOn = Instant.now();

        // When
        UserDeletedEvent event = new UserDeletedEvent(userId, newNric, occurredOn);

        // Then
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getNewNric()).isEqualTo(newNric);
        assertThat(event.getOccurredOn()).isEqualTo(occurredOn);
    }

    @Test
    void shouldGenerateUniqueEventId() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        Instant now = Instant.now();

        // When
        UserDeletedEvent event1 = new UserDeletedEvent(userId, "NRIC1", now);
        UserDeletedEvent event2 = new UserDeletedEvent(userId, "NRIC2", now);

        // Then
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void shouldReturnCorrectEventType() {
        // Given
        UserDeletedEvent event = new UserDeletedEvent(
            "test-user-" + System.currentTimeMillis(),
            "NRIC",
            Instant.now()
        );

        // When
        String eventType = event.getEventType();

        // Then
        assertThat(eventType).isEqualTo("UserDeleted");
    }

    @Test
    void shouldImplementDomainEventInterface() {
        // Given
        UserDeletedEvent event = new UserDeletedEvent(
            "test-user-" + System.currentTimeMillis(),
            "NRIC",
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

        UserDeletedEvent event = new UserDeletedEvent(userId, nric, occurredOn);

        // When
        String toString = event.toString();

        // Then
        assertThat(toString).contains("UserDeletedEvent");
        assertThat(toString).contains(userId.toString());
        assertThat(toString).contains(nric);
    }

    @Test
    void shouldHandleNullNric() {
        // When
        UserDeletedEvent event = new UserDeletedEvent(
            "test-user-" + System.currentTimeMillis(),
            null,
            Instant.now()
        );

        // Then
        assertThat(event.getNewNric()).isNull();
    }

    @Test
    void shouldPreserveOccurredOnTimestamp() {
        // Given
        Instant specificTime = Instant.parse("2024-01-15T10:30:00Z");

        // When
        UserDeletedEvent event = new UserDeletedEvent(
            "test-user-" + System.currentTimeMillis(),
            "NRIC",
            specificTime
        );

        // Then
        assertThat(event.getOccurredOn()).isEqualTo(specificTime);
    }

    @Test
    void shouldAllowMultipleDeleteEventsForDifferentUsers() {
        // Given
        String userId1 = "test-user-" + System.currentTimeMillis();
        String userId2 = "test-user-" + (System.currentTimeMillis() + 1);
        Instant now = Instant.now();

        // When
        UserDeletedEvent event1 = new UserDeletedEvent(userId1, "NRIC1", now);
        UserDeletedEvent event2 = new UserDeletedEvent(userId2, "NRIC2", now);

        // Then
        assertThat(event1.getUserId()).isNotEqualTo(event2.getUserId());
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void shouldHaveSimpleStructureWithOnlyEssentialFields() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        String nric = "950101-01-1234";
        Instant now = Instant.now();

        // When
        UserDeletedEvent event = new UserDeletedEvent(userId, nric, now);

        // Then - Only essential fields (no email, username, fullName unlike Created/Updated)
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getNewNric()).isEqualTo(nric);
        assertThat(event.getOccurredOn()).isEqualTo(now);
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventType()).isEqualTo("UserDeleted");
    }
}
