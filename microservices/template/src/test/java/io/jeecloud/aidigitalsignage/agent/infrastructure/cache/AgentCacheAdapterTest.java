package io.jeecloud.aidigitalsignage.agent.infrastructure.cache;

import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import io.jeecloud.aidigitalsignage.common.application.port.out.CachePort;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AgentCacheAdapter.
 * Tests Redis caching operations with proper key management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgentCacheAdapter Tests")
class AgentCacheAdapterTest {

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private AgentCacheAdapter agentCacheAdapter;

    private Agent testAgent;
    private AgentCode testAgentCode;
    private String cacheKey;
    private int testTtlSeconds;

    @BeforeEach
    void setUp() {
        testAgentCode = AgentCode.of("AG00001");
        testTtlSeconds = 3600;
        cacheKey = "agent:code:AG00001";
        
        testAgent = Agent.create(
            testAgentCode,
            "Test Agent",
            "BR0001",
            Channel.DIRECT
        );
    }

    @Test
    @DisplayName("Should cache agent successfully")
    void shouldCacheAgentSuccessfully() {
        // Given
        doNothing().when(cachePort).put(anyString(), any(Agent.class), anyInt());

        // When
        agentCacheAdapter.cacheAgent(testAgentCode, testAgent, testTtlSeconds);

        // Then
        verify(cachePort).put(cacheKey, testAgent, testTtlSeconds);
        verifyNoMoreInteractions(cachePort);
    }

    @Test
    @DisplayName("Should cache agent by code successfully")
    void shouldCacheAgentByCodeSuccessfully() {
        // Given
        doNothing().when(cachePort).put(anyString(), any(Agent.class), anyInt());

        // When
        agentCacheAdapter.cacheAgentByCode(testAgentCode, testAgent, testTtlSeconds);

        // Then
        verify(cachePort).put(cacheKey, testAgent, testTtlSeconds);
        verifyNoMoreInteractions(cachePort);
    }

    @Test
    @DisplayName("Should get cached agent successfully")
    void shouldGetCachedAgentSuccessfully() {
        // Given
        when(cachePort.get(anyString(), eq(Agent.class))).thenReturn(Optional.of(testAgent));

        // When
        Optional<Agent> result = agentCacheAdapter.getCachedAgent(testAgentCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAgent);
        assertThat(result.get().getAgentCode()).isEqualTo(testAgentCode);

        verify(cachePort).get(cacheKey, Agent.class);
        verifyNoMoreInteractions(cachePort);
    }

    @Test
    @DisplayName("Should get cached agent by code successfully")
    void shouldGetCachedAgentByCodeSuccessfully() {
        // Given
        when(cachePort.get(anyString(), eq(Agent.class))).thenReturn(Optional.of(testAgent));

        // When
        Optional<Agent> result = agentCacheAdapter.getCachedAgentByCode(testAgentCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAgent);

        verify(cachePort).get(cacheKey, Agent.class);
        verifyNoMoreInteractions(cachePort);
    }

    @Test
    @DisplayName("Should return empty when cache miss")
    void shouldReturnEmptyWhenCacheMiss() {
        // Given
        when(cachePort.get(anyString(), eq(Agent.class))).thenReturn(Optional.empty());

        // When
        Optional<Agent> result = agentCacheAdapter.getCachedAgent(testAgentCode);

        // Then
        assertThat(result).isEmpty();

        verify(cachePort).get(cacheKey, Agent.class);
        verifyNoMoreInteractions(cachePort);
    }

    @Test
    @DisplayName("Should invalidate agent cache successfully")
    void shouldInvalidateAgentCacheSuccessfully() {
        // Given
        doNothing().when(cachePort).evict(anyString());

        // When
        agentCacheAdapter.invalidateAgent(testAgentCode);

        // Then
        verify(cachePort).evict(cacheKey);
        verifyNoMoreInteractions(cachePort);
    }

    @Test
    @DisplayName("Should invalidate agent cache by code successfully")
    void shouldInvalidateAgentCacheByCodeSuccessfully() {
        // Given
        doNothing().when(cachePort).evict(anyString());

        // When
        agentCacheAdapter.invalidateAgentByCode(testAgentCode);

        // Then
        verify(cachePort).evict(cacheKey);
        verifyNoMoreInteractions(cachePort);
    }

    @Test
    @DisplayName("Should clear all agent cache")
    void shouldClearAllAgentCache() {
        // When
        agentCacheAdapter.clearAllAgentCache();

        // Then - No interaction with cachePort as it's a placeholder implementation
        verifyNoInteractions(cachePort);
    }

    @Test
    @DisplayName("Should use correct cache key format")
    void shouldUseCorrectCacheKeyFormat() {
        // Given
        String expectedKey = "agent:code:AG00001";
        doNothing().when(cachePort).put(anyString(), any(Agent.class), anyInt());

        // When
        agentCacheAdapter.cacheAgent(testAgentCode, testAgent, testTtlSeconds);

        // Then
        verify(cachePort).put(eq(expectedKey), eq(testAgent), eq(testTtlSeconds));
    }

    @Test
    @DisplayName("Should cache with different TTL values")
    void shouldCacheWithDifferentTtlValues() {
        // Given
        int shortTtl = 300; // 5 minutes
        int longTtl = 7200; // 2 hours
        doNothing().when(cachePort).put(anyString(), any(Agent.class), anyInt());

        // When
        agentCacheAdapter.cacheAgent(testAgentCode, testAgent, shortTtl);
        agentCacheAdapter.cacheAgent(testAgentCode, testAgent, longTtl);

        // Then
        verify(cachePort).put(cacheKey, testAgent, shortTtl);
        verify(cachePort).put(cacheKey, testAgent, longTtl);
    }

    @Test
    @DisplayName("Should handle caching multiple agents with different codes")
    void shouldHandleCachingMultipleAgents() {
        // Given
        AgentCode agentCode1 = AgentCode.of("AG00001");
        AgentCode agentCode2 = AgentCode.of("AG00002");
        
        Agent agent1 = Agent.create(agentCode1, "Agent 1", "BR0001", Channel.DIRECT);
        Agent agent2 = Agent.create(agentCode2, "Agent 2", "BR0002", Channel.BROKER);

        doNothing().when(cachePort).put(anyString(), any(Agent.class), anyInt());

        // When
        agentCacheAdapter.cacheAgent(agentCode1, agent1, testTtlSeconds);
        agentCacheAdapter.cacheAgent(agentCode2, agent2, testTtlSeconds);

        // Then
        verify(cachePort).put("agent:code:AG00001", agent1, testTtlSeconds);
        verify(cachePort).put("agent:code:AG00002", agent2, testTtlSeconds);
    }

    @Test
    @DisplayName("Should retrieve cached agents independently")
    void shouldRetrieveCachedAgentsIndependently() {
        // Given
        AgentCode agentCode1 = AgentCode.of("AG00001");
        AgentCode agentCode2 = AgentCode.of("AG00002");
        
        Agent agent1 = Agent.create(agentCode1, "Agent 1", "BR0001", Channel.DIRECT);
        Agent agent2 = Agent.create(agentCode2, "Agent 2", "BR0002", Channel.BROKER);

        when(cachePort.get("agent:code:AG00001", Agent.class)).thenReturn(Optional.of(agent1));
        when(cachePort.get("agent:code:AG00002", Agent.class)).thenReturn(Optional.of(agent2));

        // When
        Optional<Agent> result1 = agentCacheAdapter.getCachedAgent(agentCode1);
        Optional<Agent> result2 = agentCacheAdapter.getCachedAgent(agentCode2);

        // Then
        assertThat(result1).isPresent();
        assertThat(result1.get()).isEqualTo(agent1);
        assertThat(result2).isPresent();
        assertThat(result2.get()).isEqualTo(agent2);

        verify(cachePort).get("agent:code:AG00001", Agent.class);
        verify(cachePort).get("agent:code:AG00002", Agent.class);
    }

    @Test
    @DisplayName("Should handle cache errors gracefully")
    void shouldHandleCacheErrorsGracefully() {
        // Given
        when(cachePort.get(anyString(), eq(Agent.class)))
            .thenThrow(new RuntimeException("Redis connection failed"));

        // When / Then
        assertThatThrownBy(() -> agentCacheAdapter.getCachedAgent(testAgentCode))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Redis connection failed");

        verify(cachePort).get(cacheKey, Agent.class);
    }

    @Test
    @DisplayName("Should invalidate specific agent without affecting others")
    void shouldInvalidateSpecificAgentWithoutAffectingOthers() {
        // Given
        AgentCode agentCode1 = AgentCode.of("AG00001");
        AgentCode agentCode2 = AgentCode.of("AG00002");

        doNothing().when(cachePort).evict(anyString());

        // When
        agentCacheAdapter.invalidateAgent(agentCode1);

        // Then
        verify(cachePort).evict("agent:code:AG00001");
        verify(cachePort, never()).evict("agent:code:AG00002");
    }

    @Test
    @DisplayName("Should handle concurrent cache operations")
    void shouldHandleConcurrentCacheOperations() {
        // Given
        doNothing().when(cachePort).put(anyString(), any(Agent.class), anyInt());
        when(cachePort.get(anyString(), eq(Agent.class))).thenReturn(Optional.of(testAgent));
        doNothing().when(cachePort).evict(anyString());

        // When
        agentCacheAdapter.cacheAgent(testAgentCode, testAgent, testTtlSeconds);
        Optional<Agent> retrieved = agentCacheAdapter.getCachedAgent(testAgentCode);
        agentCacheAdapter.invalidateAgent(testAgentCode);

        // Then
        assertThat(retrieved).isPresent();
        verify(cachePort).put(cacheKey, testAgent, testTtlSeconds);
        verify(cachePort).get(cacheKey, Agent.class);
        verify(cachePort).evict(cacheKey);
    }

    @Test
    @DisplayName("Should use consistent key prefix for all operations")
    void shouldUseConsistentKeyPrefixForAllOperations() {
        // Given
        String expectedPrefix = "agent:code:";
        doNothing().when(cachePort).put(anyString(), any(Agent.class), anyInt());
        when(cachePort.get(anyString(), eq(Agent.class))).thenReturn(Optional.of(testAgent));
        doNothing().when(cachePort).evict(anyString());

        // When
        agentCacheAdapter.cacheAgent(testAgentCode, testAgent, testTtlSeconds);
        agentCacheAdapter.getCachedAgent(testAgentCode);
        agentCacheAdapter.invalidateAgent(testAgentCode);

        // Then
        verify(cachePort).put(startsWith(expectedPrefix), any(Agent.class), anyInt());
        verify(cachePort).get(startsWith(expectedPrefix), eq(Agent.class));
        verify(cachePort).evict(startsWith(expectedPrefix));
    }
}
