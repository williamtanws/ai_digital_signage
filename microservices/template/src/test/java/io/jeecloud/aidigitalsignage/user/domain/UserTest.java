package io.jeecloud.aidigitalsignage.user.domain;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.user.domain.event.UserCreatedEvent;
import io.jeecloud.aidigitalsignage.user.domain.event.UserUpdatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Domain Tests")
class UserTest {

    @Test
    @DisplayName("Should create user with valid data")
    void shouldCreateUserWithValidData() {
        // Given
        NewNric nric = NewNric.of("900101-01-1234");
        String email = "john@example.com";
        String name = "John Doe";

        // When
        User user = User.create(nric, email, name);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getNewNric()).isEqualTo(nric);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.isStatus()).isTrue();
        assertThat(user.getCreateDt()).isNotNull();
        assertThat(user.getUpdateDt()).isNotNull();
        assertThat(user.getAssociatedAgents()).isEmpty();
    }

    @Test
    @DisplayName("Should publish UserCreatedEvent when user is created")
    void shouldPublishUserCreatedEventWhenUserIsCreated() {
        // Given
        NewNric nric = NewNric.of("900101-01-1234");
        String email = "john@example.com";
        String name = "John Doe";

        // When
        User user = User.create(nric, email, name);

        // Then
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserCreatedEvent.class);
        
        UserCreatedEvent event = (UserCreatedEvent) user.getDomainEvents().get(0);
        assertThat(event.getUserId()).isEqualTo(user.getId().getValue());
        assertThat(event.getNewNric()).isEqualTo(nric.getValue());
        assertThat(event.getEmail()).isEqualTo(email);
        assertThat(event.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("Should update user details")
    void shouldUpdateUserDetails() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        Instant originalUpdatedAt = user.getUpdateDt();
        
        // When
        String newEmail = "john2@example.com";
        String newName = "John Doe Jr";
        user.update(newEmail, newName);

        // Then
        assertThat(user.getEmail()).isEqualTo(newEmail);
        assertThat(user.getName()).isEqualTo(newName);
        assertThat(user.getUpdateDt()).isAfterOrEqualTo(originalUpdatedAt);
    }

    @Test
    @DisplayName("Should publish UserUpdatedEvent when user is updated")
    void shouldPublishUserUpdatedEventWhenUserIsUpdated() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        user.clearDomainEvents(); // Clear creation event
        
        // When
        user.update("john2@example.com", "John Doe Jr");

        // Then
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserUpdatedEvent.class);
    }

    @Test
    @DisplayName("Should update name only")
    void shouldUpdateNameOnly() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        String originalEmail = user.getEmail();
        
        // When
        user.updateName("John Doe Jr");

        // Then
        assertThat(user.getName()).isEqualTo("John Doe Jr");
        assertThat(user.getEmail()).isEqualTo(originalEmail);
    }

    @Test
    @DisplayName("Should not update name when null or empty")
    void shouldNotUpdateNameWhenNullOrEmpty() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        String originalName = user.getName();
        
        // When & Then
        user.updateName(null);
        assertThat(user.getName()).isEqualTo(originalName);
        
        user.updateName("");
        assertThat(user.getName()).isEqualTo(originalName);
        
        user.updateName("   ");
        assertThat(user.getName()).isEqualTo(originalName);
    }

    @Test
    @DisplayName("Should update email only")
    void shouldUpdateEmailOnly() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        String originalName = user.getName();
        
        // When
        user.updateEmail("john2@example.com");

        // Then
        assertThat(user.getEmail()).isEqualTo("john2@example.com");
        assertThat(user.getName()).isEqualTo(originalName);
    }

    @Test
    @DisplayName("Should not update email when null or empty")
    void shouldNotUpdateEmailWhenNullOrEmpty() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        String originalEmail = user.getEmail();
        
        // When & Then
        user.updateEmail(null);
        assertThat(user.getEmail()).isEqualTo(originalEmail);
        
        user.updateEmail("");
        assertThat(user.getEmail()).isEqualTo(originalEmail);
        
        user.updateEmail("   ");
        assertThat(user.getEmail()).isEqualTo(originalEmail);
    }



    @Test
    @DisplayName("Should add agent to user")
    void shouldAddAgentToUser() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        AgentCode agentCode = AgentCode.of("AGT001");
        
        // When
        user.addAgent(agentCode);

        // Then
        assertThat(user.getAssociatedAgents()).contains(agentCode);
        assertThat(user.getAssociatedAgents()).hasSize(1);
    }

    @Test
    @DisplayName("Should add multiple agents to user")
    void shouldAddMultipleAgentsToUser() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        AgentCode agent1 = AgentCode.of("AGT001");
        AgentCode agent2 = AgentCode.of("AGT002");
        AgentCode agent3 = AgentCode.of("AGT003");
        
        // When
        user.addAgent(agent1);
        user.addAgent(agent2);
        user.addAgent(agent3);

        // Then
        assertThat(user.getAssociatedAgents()).contains(agent1, agent2, agent3);
        assertThat(user.getAssociatedAgents()).hasSize(3);
    }

    @Test
    @DisplayName("Should not add duplicate agents")
    void shouldNotAddDuplicateAgents() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        AgentCode agentCode = AgentCode.of("AGT001");
        
        // When
        user.addAgent(agentCode);
        user.addAgent(agentCode); // Try to add duplicate

        // Then
        assertThat(user.getAssociatedAgents()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when adding null agent")
    void shouldThrowExceptionWhenAddingNullAgent() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        
        // When & Then
        assertThatThrownBy(() -> user.addAgent(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("AgentCode cannot be null");
    }

    @Test
    @DisplayName("Should remove agent from user")
    void shouldRemoveAgentFromUser() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        AgentCode agentCode = AgentCode.of("AGT001");
        user.addAgent(agentCode);
        
        // When
        user.removeAgent(agentCode);

        // Then
        assertThat(user.getAssociatedAgents()).doesNotContain(agentCode);
        assertThat(user.getAssociatedAgents()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when removing null agent")
    void shouldThrowExceptionWhenRemovingNullAgent() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        
        // When & Then
        assertThatThrownBy(() -> user.removeAgent(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("AgentCode cannot be null");
    }

    @Test
    @DisplayName("Should activate user")
    void shouldActivateUser() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        user.deactivate(); // Deactivate first
        
        // When
        user.activate();

        // Then
        assertThat(user.isStatus()).isTrue();
    }

    @Test
    @DisplayName("Should deactivate user")
    void shouldDeactivateUser() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        
        // When
        user.deactivate();

        // Then
        assertThat(user.isStatus()).isFalse();
    }

    @Test
    @DisplayName("Should reconstitute user from persistence")
    void shouldReconstituteUserFromPersistence() {
        // Given
        UserId userId = UserId.of("test-user-" + System.currentTimeMillis());
        NewNric nric = NewNric.of("900101-01-1234");
        String email = "john@example.com";
        String name = "John Doe";
        boolean status = true;
        Set<AgentCode> agents = new HashSet<>();
        agents.add(AgentCode.of("AGT001"));
        agents.add(AgentCode.of("AGT002"));
        Instant createDt = Instant.now().minusSeconds(3600);
        Instant updateDt = Instant.now();
        
        // When
        User user = User.reconstitute(
            userId, nric, email, name, status, agents, createDt, updateDt
        );

        // Then
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getNewNric()).isEqualTo(nric);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.isStatus()).isEqualTo(status);
        assertThat(user.getAssociatedAgents()).containsExactlyInAnyOrderElementsOf(agents);
        assertThat(user.getCreateDt()).isEqualTo(createDt);
        assertThat(user.getUpdateDt()).isEqualTo(updateDt);
        assertThat(user.getDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("Should maintain equality based on ID")
    void shouldMaintainEqualityBasedOnId() {
        // Given
        UserId userId = UserId.of("test-user-" + System.currentTimeMillis());
        NewNric nric = NewNric.of("900101-01-1234");
        
        User user1 = User.reconstitute(
            userId, nric, "john@example.com", "John Doe",
            true, new HashSet<>(), Instant.now(), Instant.now()
        );
        
        User user2 = User.reconstitute(
            userId, nric, "john2@example.com", "John Doe Jr",
            true, new HashSet<>(), Instant.now(), Instant.now()
        );

        // When & Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("Should clear domain events")
    void shouldClearDomainEvents() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        assertThat(user.getDomainEvents()).isNotEmpty();
        
        // When
        user.clearDomainEvents();

        // Then
        assertThat(user.getDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("Should update timestamp when modifying user")
    void shouldUpdateTimestampWhenModifyingUser() {
        // Given
        User user = User.create(
            NewNric.of("900101-01-1234"),
            "john@example.com",
            "John Doe"
        );
        Instant originalUpdatedAt = user.getUpdateDt();
        
        // When
        try {
            Thread.sleep(10); // Ensure time passes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        user.updateName("John Doe Jr");

        // Then
        assertThat(user.getUpdateDt()).isAfter(originalUpdatedAt);
    }
}
