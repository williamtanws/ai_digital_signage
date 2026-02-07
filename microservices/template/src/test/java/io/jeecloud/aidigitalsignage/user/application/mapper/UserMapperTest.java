package io.jeecloud.aidigitalsignage.user.application.mapper;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.user.application.dto.UserResponse;
import io.jeecloud.aidigitalsignage.user.domain.NewNric;
import io.jeecloud.aidigitalsignage.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for UserMapper.
 * Tests mapper for User DTO conversions.
 */
@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    @DisplayName("Should map user to response successfully")
    void shouldMapUserToResponse() {
        // Given
        NewNric nric = NewNric.of("990101-14-5678");
        String email = "test@example.com";
        String name = "Test User";

        User user = User.create(nric, email, name);

        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(user.getId().getValue().toString());
        assertThat(response.newNric()).isEqualTo(nric.getValue());
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.name()).isEqualTo(name);
        assertThat(response.createDt()).isNotNull();
        assertThat(response.updateDt()).isNotNull();
    }

    @Test
    @DisplayName("Should map inactive user correctly")
    void shouldMapInactiveUser() {
        // Given
        User user = User.create(
            NewNric.of("990101-14-5678"),
            "test@example.com",
            "Test User"
        );
        user.deactivate();

        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should map user with agent associations")
    void shouldMapUserWithAgentAssociations() {
        // Given
        User user = User.create(
            NewNric.of("990101-14-5678"),
            "test@example.com",
            "Test User"
        );
        user.addAgent(AgentCode.of("AG00001"));
        user.addAgent(AgentCode.of("AG00002"));

        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.associatedAgents()).hasSize(2);
        assertThat(response.associatedAgents()).contains("AG00001", "AG00002");
    }

    @Test
    @DisplayName("Should map user without agent associations")
    void shouldMapUserWithoutAgentAssociations() {
        // Given
        User user = User.create(
            NewNric.of("990101-14-5678"),
            "test@example.com",
            "Test User"
        );

        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.associatedAgents()).isEmpty();
    }

    @Test
    @DisplayName("Should map list of users to response list")
    void shouldMapListOfUsersToResponseList() {
        // Given
        User user1 = User.create(
            NewNric.of("990101-14-5678"),
            "user1@example.com",
            "User One"
        );
        User user2 = User.create(
            NewNric.of("980202-15-6789"),
            "user2@example.com",
            "User Two"
        );
        User user3 = User.create(
            NewNric.of("970303-16-7890"),
            "user3@example.com",
            "User Three"
        );

        List<User> users = Arrays.asList(user1, user2, user3);

        // When
        List<UserResponse> responses = userMapper.toResponseList(users);

        // Then
        assertThat(responses).hasSize(3);
        assertThat(responses).extracting(UserResponse::email)
            .containsExactly("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    @DisplayName("Should map empty list of users")
    void shouldMapEmptyListOfUsers() {
        // Given
        List<User> users = List.of();

        // When
        List<UserResponse> responses = userMapper.toResponseList(users);

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Should preserve user ID in mapping")
    void shouldPreserveUserIdInMapping() {
        // Given
        User user = User.create(
            NewNric.of("990101-14-5678"),
            "test@example.com",
            "Test User"
        );

        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertThat(response.userId()).isEqualTo(user.getId().getValue().toString());
    }

    @Test
    @DisplayName("Should preserve timestamps in mapping")
    void shouldPreserveTimestampsInMapping() {
        // Given
        User user = User.create(
            NewNric.of("990101-14-5678"),
            "test@example.com",
            "Test User"
        );

        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertThat(response.createDt()).isEqualTo(user.getCreateDt());
        assertThat(response.updateDt()).isEqualTo(user.getUpdateDt());
    }

    @Test
    @DisplayName("Should map updated user details")
    void shouldMapUpdatedUserDetails() {
        // Given
        User user = User.create(
            NewNric.of("990101-14-5678"),
            "test@example.com",
            "Test User"
        );
        user.update("updated@example.com", "Updated User");

        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertThat(response.email()).isEqualTo("updated@example.com");
        assertThat(response.name()).isEqualTo("Updated User");
    }

    @Test
    @DisplayName("Should handle NRIC value object correctly")
    void shouldHandleNricValueObject() {
        // Given
        NewNric nric = NewNric.of("990101-14-5678");
        User user = User.create(nric, "test@example.com", "Test User");

        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertThat(response.newNric()).isEqualTo(nric.getValue());
        assertThat(response.newNric()).isEqualTo("990101-14-5678");
    }

    @Test
    @DisplayName("Should map multiple users independently")
    void shouldMapMultipleUsersIndependently() {
        // Given
        User user1 = User.create(
            NewNric.of("990101-14-5678"),
            "user1@example.com",
            "User One"
        );
        user1.addAgent(AgentCode.of("AG00001"));

        User user2 = User.create(
            NewNric.of("980202-15-6789"),
            "user2@example.com",
            "User Two"
        );
        user2.addAgent(AgentCode.of("AG00002"));

        // When
        UserResponse response1 = userMapper.toResponse(user1);
        UserResponse response2 = userMapper.toResponse(user2);

        // Then
        assertThat(response1.associatedAgents()).containsOnly("AG00001");

        assertThat(response2.associatedAgents()).containsOnly("AG00002");
    }

    @Test
    @DisplayName("Should map list preserving order")
    void shouldMapListPreservingOrder() {
        // Given
        User user1 = User.create(NewNric.of("990101-14-5678"), "user1@example.com", "User One");
        User user2 = User.create(NewNric.of("980202-15-6789"), "user2@example.com", "User Two");
        User user3 = User.create(NewNric.of("970303-16-7890"), "user3@example.com", "User Three");

        List<User> users = Arrays.asList(user1, user2, user3);

        // When
        List<UserResponse> responses = userMapper.toResponseList(users);

        // Then
        assertThat(responses.get(0).name()).isEqualTo("User One");
        assertThat(responses.get(1).name()).isEqualTo("User Two");
        assertThat(responses.get(2).name()).isEqualTo("User Three");
    }
}
