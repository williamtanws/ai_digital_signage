package io.jeecloud.aidigitalsignage.user.infrastructure.persistence;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.user.domain.NewNric;
import io.jeecloud.aidigitalsignage.user.domain.User;
import io.jeecloud.aidigitalsignage.user.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserRepositoryAdapter.
 * Tests repository adapter pattern bridging domain and persistence layers.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepositoryAdapter Tests")
class UserRepositoryAdapterTest {

    @Mock
    private UserEntityManagerRepository userEntityManagerRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private UserAgentEntityManagerRepository userAgentEntityManagerRepository;

    @Mock
    private UserAgentJpaRepository userAgentJpaRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    private User testUser;
    private UserEntity testEntity;
    private UserId testUserId;
    private NewNric testNric;
    private String testEmail;
    private AgentCode testAgentCode;

    @BeforeEach
    void setUp() {
        testUserId = UserId.of("test-user-" + System.currentTimeMillis());
        testNric = NewNric.of("990101-14-5678");
        testEmail = "test@example.com";
        testAgentCode = AgentCode.of("AG00001");
        
        testUser = User.reconstitute(testUserId, testNric, testEmail, "Test User", true, Set.of(), Instant.now(), Instant.now());
        
        testEntity = UserEntity.builder()
            .userId(testUserId.getValue())
            .newNric(testNric.getValue())
            .email(testEmail)
            .name("Test User")
            .status(true)
            .createDt(Instant.now())
            .updateDt(Instant.now())
            .build();
    }

    @Test
    @DisplayName("Should save user successfully using JPA repository")
    void shouldSaveUserSuccessfully() {
        // Given
        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(testEntity);
        when(userAgentEntityManagerRepository.findByUserId(any(String.class))).thenReturn(List.of());

        // When
        User savedUser = userRepositoryAdapter.save(testUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(testEmail);

        verify(userJpaRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should save user with agent associations")
    void shouldSaveUserWithAgentAssociations() {
        // Given
        testUser.addAgent(testAgentCode);
        UserAgentEntity.UserAgentId userAgentId = new UserAgentEntity.UserAgentId(testUser.getId().getValue(), testAgentCode.value());
        UserAgentEntity userAgentEntity = UserAgentEntity.builder()
            .id(userAgentId)
            .build();
        
        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(testEntity);
        when(userAgentEntityManagerRepository.findByUserId(any(String.class)))
            .thenReturn(List.of())
            .thenReturn(List.of(userAgentEntity));

        // When
        User savedUser = userRepositoryAdapter.save(testUser);

        // Then
        assertThat(savedUser).isNotNull();

        verify(userJpaRepository).save(any(UserEntity.class));
        verify(userAgentJpaRepository).deleteByUserId(testUser.getId().getValue());
        verify(userAgentEntityManagerRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should find user by ID using EntityManager repository")
    void shouldFindUserById() {
        // Given
        when(userEntityManagerRepository.findById(any(String.class))).thenReturn(Optional.of(testEntity));
        when(userAgentEntityManagerRepository.findByUserId(any(String.class))).thenReturn(List.of());

        // When
        Optional<User> result = userRepositoryAdapter.findById(testUserId);

        // Then
        assertThat(result).isPresent();

        verify(userEntityManagerRepository).findById(testUserId.getValue());
        verify(userAgentEntityManagerRepository).findByUserId(testUserId.getValue());
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // Given
        UserId nonExistentId = UserId.of("test-user-" + System.currentTimeMillis());
        when(userEntityManagerRepository.findById(any(String.class))).thenReturn(Optional.empty());

        // When
        Optional<User> result = userRepositoryAdapter.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();

        verify(userEntityManagerRepository).findById(nonExistentId.getValue());
    }

    @Test
    @DisplayName("Should find user by NRIC using JPA repository")
    void shouldFindUserByNric() {
        // Given
        when(userJpaRepository.findByNewNric(testNric.getValue())).thenReturn(Optional.of(testEntity));
        when(userAgentEntityManagerRepository.findByUserId(any(String.class))).thenReturn(List.of());

        // When
        Optional<User> result = userRepositoryAdapter.findByNewNric(testNric);

        // Then
        assertThat(result).isPresent();

        verify(userJpaRepository).findByNewNric(testNric.getValue());
    }

    @Test
    @DisplayName("Should return empty when user not found by NRIC")
    void shouldReturnEmptyWhenUserNotFoundByNric() {
        // Given
        NewNric nonExistentNric = NewNric.of("991212-14-9999");
        when(userJpaRepository.findByNewNric(nonExistentNric.getValue())).thenReturn(Optional.empty());

        // When
        Optional<User> result = userRepositoryAdapter.findByNewNric(nonExistentNric);

        // Then
        assertThat(result).isEmpty();

        verify(userJpaRepository).findByNewNric(nonExistentNric.getValue());
    }

    @Test
    @DisplayName("Should find user by email using JPA repository")
    void shouldFindUserByEmail() {
        // Given
        when(userJpaRepository.findByEmail(testEmail)).thenReturn(Optional.of(testEntity));
        when(userAgentEntityManagerRepository.findByUserId(any(String.class))).thenReturn(List.of());

        // When
        Optional<User> result = userRepositoryAdapter.findByEmail(testEmail);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(testEmail);

        verify(userJpaRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(userJpaRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userRepositoryAdapter.findByEmail(nonExistentEmail);

        // Then
        assertThat(result).isEmpty();

        verify(userJpaRepository).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("Should find users by agent code")
    void shouldFindUsersByAgentCode() {
        // Given
        UserAgentEntity.UserAgentId id1 = new UserAgentEntity.UserAgentId(testUserId.getValue(), testAgentCode.value());
        UserAgentEntity association1 = UserAgentEntity.builder().id(id1).build();

        when(userAgentJpaRepository.findByAgentCode(testAgentCode.value())).thenReturn(List.of(association1));
        when(userEntityManagerRepository.findById(testUserId.getValue())).thenReturn(Optional.of(testEntity));
        when(userAgentEntityManagerRepository.findByUserId(any(String.class))).thenReturn(List.of(association1));

        // When
        List<User> result = userRepositoryAdapter.findByAgentCode(testAgentCode);

        // Then
        assertThat(result).hasSize(1);

        verify(userAgentJpaRepository).findByAgentCode(testAgentCode.value());
    }

    @Test
    @DisplayName("Should return empty list when no users by agent code")
    void shouldReturnEmptyListWhenNoUsersByAgentCode() {
        // Given
        AgentCode nonExistentCode = AgentCode.of("AG99999");
        when(userAgentJpaRepository.findByAgentCode(nonExistentCode.value())).thenReturn(List.of());

        // When
        List<User> result = userRepositoryAdapter.findByAgentCode(nonExistentCode);

        // Then
        assertThat(result).isEmpty();

        verify(userAgentJpaRepository).findByAgentCode(nonExistentCode.value());
    }

    @Test
    @DisplayName("Should find all active users using EntityManager repository")
    void shouldFindAllActiveUsers() {
        // Given
        UserEntity entity2 = UserEntity.builder()
            .userId("test-user-" + System.currentTimeMillis())
            .newNric("980202-15-6789")
            .email("test2@example.com")
            .name("Test User 2")
            .status(true)
            .createDt(Instant.now())
            .updateDt(Instant.now())
            .build();

        when(userEntityManagerRepository.findAllActive()).thenReturn(Arrays.asList(testEntity, entity2));
        when(userAgentEntityManagerRepository.findByUserId(any(String.class))).thenReturn(List.of());

        // When
        List<User> result = userRepositoryAdapter.findAllActive();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(User::isStatus);

        verify(userEntityManagerRepository).findAllActive();
    }

    @Test
    @DisplayName("Should return empty list when no active users exist")
    void shouldReturnEmptyListWhenNoActiveUsers() {
        // Given
        when(userEntityManagerRepository.findAllActive()).thenReturn(List.of());

        // When
        List<User> result = userRepositoryAdapter.findAllActive();

        // Then
        assertThat(result).isEmpty();

        verify(userEntityManagerRepository).findAllActive();
    }

    @Test
    @DisplayName("Should find all users using JPA repository")
    void shouldFindAllUsers() {
        // Given
        UserEntity inactiveEntity = UserEntity.builder()
            .userId("test-user-" + System.currentTimeMillis())
            .newNric("970303-16-7890")
            .email("inactive@example.com")
            .name("Inactive User")
            .status(false)
            .createDt(Instant.now())
            .updateDt(Instant.now())
            .build();

        when(userJpaRepository.findAll()).thenReturn(Arrays.asList(testEntity, inactiveEntity));
        when(userAgentEntityManagerRepository.findByUserId(any(String.class))).thenReturn(List.of());

        // When
        List<User> result = userRepositoryAdapter.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::isStatus).contains(true, false);

        verify(userJpaRepository).findAll();
    }

    @Test
    @DisplayName("Should delete user by ID and cascade delete associations")
    void shouldDeleteUserById() {
        // Given
        doNothing().when(userAgentEntityManagerRepository).deleteByUserId(any(String.class));
        doNothing().when(userJpaRepository).deleteById(any(String.class));

        // When
        userRepositoryAdapter.deleteById(testUserId);

        // Then
        verify(userAgentEntityManagerRepository).deleteByUserId(testUserId.getValue());
        verify(userJpaRepository).deleteById(testUserId.getValue());
    }

    @Test
    @DisplayName("Should check if user exists by NRIC")
    void shouldCheckIfUserExistsByNric() {
        // Given
        when(userJpaRepository.existsByNewNric(testNric.getValue())).thenReturn(true);

        // When
        boolean exists = userRepositoryAdapter.existsByNewNric(testNric);

        // Then
        assertThat(exists).isTrue();

        verify(userJpaRepository).existsByNewNric(testNric.getValue());
    }

    @Test
    @DisplayName("Should return false when user does not exist by NRIC")
    void shouldReturnFalseWhenUserDoesNotExistByNric() {
        // Given
        NewNric nonExistentNric = NewNric.of("991212-14-9999");
        when(userJpaRepository.existsByNewNric(nonExistentNric.getValue())).thenReturn(false);

        // When
        boolean exists = userRepositoryAdapter.existsByNewNric(nonExistentNric);

        // Then
        assertThat(exists).isFalse();

        verify(userJpaRepository).existsByNewNric(nonExistentNric.getValue());
    }

    @Test
    @DisplayName("Should save user-agent associations")
    void shouldSaveUserAgentAssociations() {
        // Given
        Set<AgentCode> agentCodes = Set.of(testAgentCode, AgentCode.of("AG00002"));
        doNothing().when(userAgentJpaRepository).deleteByUserId(any(String.class));
        when(userAgentEntityManagerRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        userRepositoryAdapter.saveUserAgentAssociations(testUserId, agentCodes);

        // Then
        verify(userAgentJpaRepository).deleteByUserId(testUserId.getValue());
        verify(userAgentEntityManagerRepository).saveAll(argThat(list -> list.size() == 2));
    }

    @Test
    @DisplayName("Should load user-agent associations")
    void shouldLoadUserAgentAssociations() {
        // Given
        UserAgentEntity.UserAgentId id1 = new UserAgentEntity.UserAgentId(testUserId.getValue(), testAgentCode.value());
        UserAgentEntity.UserAgentId id2 = new UserAgentEntity.UserAgentId(testUserId.getValue(), "AG00002");
        
        UserAgentEntity association1 = UserAgentEntity.builder().id(id1).build();
        UserAgentEntity association2 = UserAgentEntity.builder().id(id2).build();

        when(userAgentEntityManagerRepository.findByUserId(testUserId.getValue()))
            .thenReturn(Arrays.asList(association1, association2));

        // When
        Set<AgentCode> result = userRepositoryAdapter.loadUserAgentAssociations(testUserId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(testAgentCode, AgentCode.of("AG00002"));

        verify(userAgentEntityManagerRepository).findByUserId(testUserId.getValue());
    }

    @Test
    @DisplayName("Should return empty set when user has no agent associations")
    void shouldReturnEmptySetWhenNoAgentAssociations() {
        // Given
        when(userAgentEntityManagerRepository.findByUserId(testUserId.getValue())).thenReturn(List.of());

        // When
        Set<AgentCode> result = userRepositoryAdapter.loadUserAgentAssociations(testUserId);

        // Then
        assertThat(result).isEmpty();

        verify(userAgentEntityManagerRepository).findByUserId(testUserId.getValue());
    }
}
