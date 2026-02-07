package io.jeecloud.aidigitalsignage.user.application.service;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.user.application.port.in.FindUserUseCase;
import io.jeecloud.aidigitalsignage.user.application.port.in.FindUserUseCase.FindByAgentCodeQuery;
import io.jeecloud.aidigitalsignage.user.application.port.in.FindUserUseCase.FindByIdQuery;
import io.jeecloud.aidigitalsignage.user.application.port.in.FindUserUseCase.FindByNricQuery;
import io.jeecloud.aidigitalsignage.user.application.port.in.FindUserUseCase.FindAllActiveQuery;
import io.jeecloud.aidigitalsignage.user.application.port.out.UserCachePort;
import io.jeecloud.aidigitalsignage.user.domain.NewNric;
import io.jeecloud.aidigitalsignage.user.domain.User;
import io.jeecloud.aidigitalsignage.user.domain.UserId;
import io.jeecloud.aidigitalsignage.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserQueryService.
 * Tests query side of CQRS with caching support.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserQueryService Tests")
class UserQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCachePort cachePort;

    @InjectMocks
    private UserQueryService userQueryService;

    private User testUser1;
    private User testUser2;
    private UserId testUserId1;
    private UserId testUserId2;
    private String testNric1;
    private String testNric2;
    private AgentCode testAgentCode;

    @BeforeEach
    void setUp() {
        testUserId1 = UserId.of("test-user-" + System.currentTimeMillis());
        testUserId2 = UserId.of("test-user-" + System.currentTimeMillis());
        testNric1 = "990101-14-5678";
        testNric2 = "980202-15-6789";
        testAgentCode = AgentCode.of("AG00001");
        
        testUser1 = User.create(
            NewNric.of(testNric1),
            "test1@example.com",
            "Test User 1"
        );
        
        testUser2 = User.create(
            NewNric.of(testNric2),
            "test2@example.com",
            "Test User 2"
        );
    }

    @Test
    @DisplayName("Should find user by ID from cache when available")
    void shouldFindUserByIdFromCache() {
        // Given
        String userId = testUserId1.getValue().toString();
        FindByIdQuery query = new FindByIdQuery(userId);

        when(cachePort.findById(any(UserId.class))).thenReturn(Optional.of(testUser1));

        // When
        Optional<User> result = userQueryService.findById(query);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser1);

        verify(cachePort).findById(any(UserId.class));
        verify(userRepository, never()).findById(any(UserId.class));
        verify(cachePort, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should find user by ID from repository when cache miss")
    void shouldFindUserByIdFromRepositoryOnCacheMiss() {
        // Given
        String userId = testUserId1.getValue().toString();
        FindByIdQuery query = new FindByIdQuery(userId);

        when(cachePort.findById(any(UserId.class))).thenReturn(Optional.empty());
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser1));

        // When
        Optional<User> result = userQueryService.findById(query);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser1);

        verify(cachePort).findById(any(UserId.class));
        verify(userRepository).findById(any(UserId.class));
        verify(cachePort).save(testUser1);
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // Given
        String userId = "test-user-" + System.currentTimeMillis();
        FindByIdQuery query = new FindByIdQuery(userId);

        when(cachePort.findById(any(UserId.class))).thenReturn(Optional.empty());
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        // When
        Optional<User> result = userQueryService.findById(query);

        // Then
        assertThat(result).isEmpty();

        verify(cachePort).findById(any(UserId.class));
        verify(userRepository).findById(any(UserId.class));
        verify(cachePort, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should find user by NRIC and update cache")
    void shouldFindUserByNricAndUpdateCache() {
        // Given
        FindByNricQuery query = new FindByNricQuery(testNric1);

        when(userRepository.findByNewNric(any(NewNric.class))).thenReturn(Optional.of(testUser1));

        // When
        Optional<User> result = userQueryService.findByNric(query);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser1);

        verify(userRepository).findByNewNric(any(NewNric.class));
        verify(cachePort).save(testUser1);
    }

    @Test
    @DisplayName("Should return empty when user not found by NRIC")
    void shouldReturnEmptyWhenUserNotFoundByNric() {
        // Given
        FindByNricQuery query = new FindByNricQuery("991212-14-9999");

        when(userRepository.findByNewNric(any(NewNric.class))).thenReturn(Optional.empty());

        // When
        Optional<User> result = userQueryService.findByNric(query);

        // Then
        assertThat(result).isEmpty();

        verify(userRepository).findByNewNric(any(NewNric.class));
        verify(cachePort, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should find users by agent code")
    void shouldFindUsersByAgentCode() {
        // Given
        String agentCode = testAgentCode.value();
        FindByAgentCodeQuery query = new FindByAgentCodeQuery(agentCode);

        List<User> expectedUsers = Arrays.asList(testUser1, testUser2);
        when(userRepository.findByAgentCode(any(AgentCode.class))).thenReturn(expectedUsers);

        // When
        List<User> result = userQueryService.findByAgentCode(query);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testUser1, testUser2);

        verify(userRepository).findByAgentCode(any(AgentCode.class));
    }

    @Test
    @DisplayName("Should return empty list when no users found by agent code")
    void shouldReturnEmptyListWhenNoUsersByAgentCode() {
        // Given
        String agentCode = "AG99999";
        FindByAgentCodeQuery query = new FindByAgentCodeQuery(agentCode);

        when(userRepository.findByAgentCode(any(AgentCode.class))).thenReturn(List.of());

        // When
        List<User> result = userQueryService.findByAgentCode(query);

        // Then
        assertThat(result).isEmpty();

        verify(userRepository).findByAgentCode(any(AgentCode.class));
    }

    @Test
    @DisplayName("Should find all active users")
    void shouldFindAllActiveUsers() {
        // Given
        FindAllActiveQuery query = new FindAllActiveQuery();

        List<User> activeUsers = Arrays.asList(testUser1, testUser2);
        when(userRepository.findAllActive()).thenReturn(activeUsers);

        // When
        List<User> result = userQueryService.findAllActive(query);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testUser1, testUser2);

        verify(userRepository).findAllActive();
    }

    @Test
    @DisplayName("Should return empty list when no active users exist")
    void shouldReturnEmptyListWhenNoActiveUsers() {
        // Given
        FindAllActiveQuery query = new FindAllActiveQuery();

        when(userRepository.findAllActive()).thenReturn(List.of());

        // When
        List<User> result = userQueryService.findAllActive(query);

        // Then
        assertThat(result).isEmpty();

        verify(userRepository).findAllActive();
    }

    @Test
    @DisplayName("Should verify cache is not updated for list queries")
    void shouldNotUpdateCacheForListQueries() {
        // Given
        FindByAgentCodeQuery agentQuery = new FindByAgentCodeQuery("AG00001");
        FindAllActiveQuery activeQuery = new FindAllActiveQuery();

        when(userRepository.findByAgentCode(any(AgentCode.class))).thenReturn(Arrays.asList(testUser1));
        when(userRepository.findAllActive()).thenReturn(Arrays.asList(testUser1, testUser2));

        // When
        userQueryService.findByAgentCode(agentQuery);
        userQueryService.findAllActive(activeQuery);

        // Then - cache should never be updated for list queries
        verify(cachePort, never()).save(any(User.class));
    }
}
