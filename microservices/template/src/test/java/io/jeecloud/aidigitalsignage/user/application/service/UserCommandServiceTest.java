package io.jeecloud.aidigitalsignage.user.application.service;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;
import io.jeecloud.aidigitalsignage.user.application.port.in.CreateUserUseCase.CreateUserCommand;
import io.jeecloud.aidigitalsignage.user.application.port.in.DeleteUserUseCase.DeleteUserCommand;
import io.jeecloud.aidigitalsignage.user.application.port.in.ManageUserAgentUseCase.AddAgentCommand;
import io.jeecloud.aidigitalsignage.user.application.port.in.ManageUserAgentUseCase.RemoveAgentCommand;
import io.jeecloud.aidigitalsignage.user.application.port.in.UpdateUserUseCase.UpdateUserCommand;
import io.jeecloud.aidigitalsignage.user.application.port.out.UserCachePort;
import io.jeecloud.aidigitalsignage.user.application.port.out.UserEventPublisher;
import io.jeecloud.aidigitalsignage.user.domain.NewNric;
import io.jeecloud.aidigitalsignage.user.domain.User;
import io.jeecloud.aidigitalsignage.user.domain.UserId;
import io.jeecloud.aidigitalsignage.user.domain.UserRepository;
import io.jeecloud.aidigitalsignage.user.domain.event.UserCreatedEvent;
import io.jeecloud.aidigitalsignage.user.domain.event.UserDeletedEvent;
import io.jeecloud.aidigitalsignage.user.domain.event.UserUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserCommandService.
 * Tests command side of CQRS (Create, Update, Delete, Manage Agents).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserCommandService Tests")
class UserCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventPublisher eventPublisher;

    @Mock
    private UserCachePort cachePort;

    @InjectMocks
    private UserCommandService userCommandService;

    private User testUser;
    private UserId testUserId;
    private String testEmail;
    private String testNric;
    private String testName;

    @BeforeEach
    void setUp() {
        testUserId = UserId.of("test-user-" + System.currentTimeMillis());
        testEmail = "test@example.com";
        testNric = "990101-14-5678";
        testName = "Test User";
        
        testUser = User.create(
            NewNric.of(testNric),
            testEmail,
            testName
        );
    }

    @Test
    @DisplayName("Should create user successfully with valid data")
    void shouldCreateUserSuccessfully() {
        // Given
        CreateUserCommand command = new CreateUserCommand(
            testNric,
            testEmail,
            testName
        );

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userRepository.findByNewNric(any(NewNric.class))).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User createdUser = userCommandService.createUser(command);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo(testEmail);
        assertThat(createdUser.getName()).isEqualTo(testName);
        
        verify(userRepository).findByEmail(testEmail);
        verify(userRepository).findByNewNric(any(NewNric.class));
        verify(userRepository).save(any(User.class));
        verify(eventPublisher, atLeastOnce()).publishUserCreated(any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        CreateUserCommand command = new CreateUserCommand(
            testNric,
            testEmail,
            testName
        );

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When / Then
        assertThatThrownBy(() -> userCommandService.createUser(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("User with email " + testEmail + " already exists");

        verify(userRepository).findByEmail(testEmail);
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishUserCreated(any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when NRIC already exists")
    void shouldThrowExceptionWhenNricExists() {
        // Given
        CreateUserCommand command = new CreateUserCommand(
            testNric,
            testEmail,
            testName
        );

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userRepository.findByNewNric(any(NewNric.class))).thenReturn(Optional.of(testUser));

        // When / Then
        assertThatThrownBy(() -> userCommandService.createUser(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("User with NRIC " + testNric + " already exists");

        verify(userRepository).findByEmail(testEmail);
        verify(userRepository).findByNewNric(any(NewNric.class));
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishUserCreated(any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Given
        String userId = testUserId.getValue().toString();
        String newEmail = "updated@example.com";
        String newName = "Updated User";

        UpdateUserCommand command = new UpdateUserCommand(
            userId,
            newEmail,
            newName
        );

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userCommandService.updateUser(command);

        // Then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
        assertThat(updatedUser.getName()).isEqualTo(newName);

        verify(userRepository).findById(any(UserId.class));
        verify(userRepository).save(any(User.class));
        verify(cachePort).invalidate(any(UserId.class));
        verify(eventPublisher, atLeastOnce()).publishUserCreated(any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        UpdateUserCommand command = new UpdateUserCommand(
            userId,
            "new@example.com",
            "New User"
        );

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userCommandService.updateUser(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findById(any(UserId.class));
        verify(userRepository, never()).save(any(User.class));
        verify(cachePort, never()).invalidate(any(UserId.class));
        verify(eventPublisher, never()).publishUserUpdated(any(UserUpdatedEvent.class));
    }

    @Test
    @DisplayName("Should update only provided fields")
    void shouldUpdateOnlyProvidedFields() {
        // Given
        String userId = testUserId.getValue().toString();
        String newName = "Updated User";

        UpdateUserCommand command = new UpdateUserCommand(
            userId,
            testEmail,  // keep same email
            newName
        );

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userCommandService.updateUser(command);

        // Then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo(newName);
        assertThat(updatedUser.getEmail()).isEqualTo(testEmail);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully (soft delete)")
    void shouldDeleteUserSuccessfully() {
        // Given
        String userId = testUserId.getValue().toString();
        DeleteUserCommand command = new DeleteUserCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userCommandService.deleteUser(command);

        // Then
        assertThat(testUser.isStatus()).isFalse();

        verify(userRepository).findById(any(UserId.class));
        verify(userRepository).save(any(User.class));
        verify(cachePort).invalidate(any(UserId.class));
        verify(eventPublisher, atLeastOnce()).publishUserCreated(any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        DeleteUserCommand command = new DeleteUserCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userCommandService.deleteUser(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findById(any(UserId.class));
        verify(userRepository, never()).save(any(User.class));
        verify(cachePort, never()).invalidate(any(UserId.class));
        verify(eventPublisher, never()).publishUserDeleted(any(UserDeletedEvent.class));
    }

    @Test
    @DisplayName("Should add agent to user successfully")
    void shouldAddAgentToUserSuccessfully() {
        // Given
        String userId = testUserId.getValue().toString();
        String agentCode = "AG00001";
        AddAgentCommand command = new AddAgentCommand(userId, agentCode);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userCommandService.addAgentToUser(command);

        // Then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getAssociatedAgents()).contains(AgentCode.of(agentCode));

        verify(userRepository).findById(any(UserId.class));
        verify(userRepository).save(any(User.class));
        verify(cachePort).invalidate(any(UserId.class));
        verify(eventPublisher, atLeastOnce()).publishUserCreated(any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when adding agent to non-existent user")
    void shouldThrowExceptionWhenAddingAgentToNonExistentUser() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        String agentCode = "AG00001";
        AddAgentCommand command = new AddAgentCommand(userId, agentCode);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userCommandService.addAgentToUser(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findById(any(UserId.class));
        verify(userRepository, never()).save(any(User.class));
        verify(cachePort, never()).invalidate(any(UserId.class));
    }

    @Test
    @DisplayName("Should remove agent from user successfully")
    void shouldRemoveAgentFromUserSuccessfully() {
        // Given
        String userId = testUserId.getValue().toString();
        String agentCode = "AG00001";
        
        // First add agent
        testUser.addAgent(AgentCode.of(agentCode));
        
        RemoveAgentCommand command = new RemoveAgentCommand(userId, agentCode);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userCommandService.removeAgentFromUser(command);

        // Then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getAssociatedAgents()).doesNotContain(AgentCode.of(agentCode));

        verify(userRepository).findById(any(UserId.class));
        verify(userRepository).save(any(User.class));
        verify(cachePort).invalidate(any(UserId.class));
        verify(eventPublisher, atLeastOnce()).publishUserCreated(any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when removing agent from non-existent user")
    void shouldThrowExceptionWhenRemovingAgentFromNonExistentUser() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        String agentCode = "AG00001";
        RemoveAgentCommand command = new RemoveAgentCommand(userId, agentCode);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userCommandService.removeAgentFromUser(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findById(any(UserId.class));
        verify(userRepository, never()).save(any(User.class));
        verify(cachePort, never()).invalidate(any(UserId.class));
    }
}
