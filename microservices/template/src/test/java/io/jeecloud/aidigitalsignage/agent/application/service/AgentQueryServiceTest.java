package io.jeecloud.aidigitalsignage.agent.application.service;

import io.jeecloud.aidigitalsignage.agent.application.port.out.AgentCachePort;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.agent.domain.AgentRepository;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AgentQueryService.
 * Tests query side of CQRS with caching support.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgentQueryService Tests")
class AgentQueryServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AgentCachePort cachePort;

    @InjectMocks
    private AgentQueryService agentQueryService;

    private Agent testAgent1;
    private Agent testAgent2;
    private AgentCode testAgentCode1;
    private AgentCode testAgentCode2;
    private String testBranchCode;

    @BeforeEach
    void setUp() {
        testAgentCode1 = AgentCode.of("AG00001");
        testAgentCode2 = AgentCode.of("AG00002");
        testBranchCode = "BR0001";
        
        testAgent1 = Agent.create(
            testAgentCode1,
            "Test Agent 1",
            testBranchCode,
            Channel.DIRECT
        );
        
        testAgent2 = Agent.create(
            testAgentCode2,
            "Test Agent 2",
            testBranchCode,
            Channel.BROKER
        );
    }

    @Test
    @DisplayName("Should find agent by agent code")
    void shouldFindAgentByAgentCode() {
        // Given
        String agentCodeStr = testAgentCode1.value();
        when(cachePort.getCachedAgentByCode(any(AgentCode.class))).thenReturn(Optional.empty());
        when(agentRepository.findByAgentCode(any(AgentCode.class))).thenReturn(Optional.of(testAgent1));

        // When
        Optional<Agent> result = agentQueryService.findByAgentCode(agentCodeStr);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAgentCode()).isEqualTo(testAgentCode1);
        verify(cachePort).getCachedAgentByCode(any(AgentCode.class));
        verify(agentRepository).findByAgentCode(any(AgentCode.class));
        verify(cachePort).cacheAgentByCode(any(AgentCode.class), eq(testAgent1), anyInt());
    }

    @Test
    @DisplayName("Should find agent by code from cache when available")
    void shouldFindAgentByCodeFromCache() {
        // Given
        String agentCode = testAgentCode1.value();

        when(cachePort.getCachedAgentByCode(any(AgentCode.class))).thenReturn(Optional.of(testAgent1));

        // When
        Optional<Agent> result = agentQueryService.findByAgentCode(agentCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAgent1);
        assertThat(result.get().getAgentCode()).isEqualTo(testAgentCode1);

        verify(cachePort).getCachedAgentByCode(any(AgentCode.class));
        verify(agentRepository, never()).findByAgentCode(any(AgentCode.class));
        verify(cachePort, never()).cacheAgentByCode(any(AgentCode.class), any(Agent.class), anyInt());
    }

    @Test
    @DisplayName("Should find agent by code from repository when cache miss")
    void shouldFindAgentByCodeFromRepositoryOnCacheMiss() {
        // Given
        String agentCode = testAgentCode1.value();

        when(cachePort.getCachedAgentByCode(any(AgentCode.class))).thenReturn(Optional.empty());
        when(agentRepository.findByAgentCode(any(AgentCode.class))).thenReturn(Optional.of(testAgent1));

        // When
        Optional<Agent> result = agentQueryService.findByAgentCode(agentCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAgent1);
        assertThat(result.get().getAgentCode()).isEqualTo(testAgentCode1);

        verify(cachePort).getCachedAgentByCode(any(AgentCode.class));
        verify(agentRepository).findByAgentCode(any(AgentCode.class));
        verify(cachePort).cacheAgentByCode(any(AgentCode.class), eq(testAgent1), anyInt());
    }

    @Test
    @DisplayName("Should return empty when agent not found by code")
    void shouldReturnEmptyWhenAgentNotFoundByCode() {
        // Given
        String agentCode = "AG99999";

        when(cachePort.getCachedAgentByCode(any(AgentCode.class))).thenReturn(Optional.empty());
        when(agentRepository.findByAgentCode(any(AgentCode.class))).thenReturn(Optional.empty());

        // When
        Optional<Agent> result = agentQueryService.findByAgentCode(agentCode);

        // Then
        assertThat(result).isEmpty();

        verify(cachePort).getCachedAgentByCode(any(AgentCode.class));
        verify(agentRepository).findByAgentCode(any(AgentCode.class));
        verify(cachePort, never()).cacheAgentByCode(any(AgentCode.class), any(Agent.class), anyInt());
    }

    @Test
    @DisplayName("Should find agents by branch code")
    void shouldFindAgentsByBranchCode() {
        // Given
        List<Agent> expectedAgents = Arrays.asList(testAgent1, testAgent2);

        when(agentRepository.findByBranchCode(testBranchCode)).thenReturn(expectedAgents);

        // When
        List<Agent> result = agentQueryService.findByBranchCode(testBranchCode);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testAgent1, testAgent2);

        verify(agentRepository).findByBranchCode(testBranchCode);
    }

    @Test
    @DisplayName("Should return empty list when no agents found by branch code")
    void shouldReturnEmptyListWhenNoAgentsFoundByBranchCode() {
        // Given
        String nonExistentBranchCode = "BR9999";

        when(agentRepository.findByBranchCode(nonExistentBranchCode)).thenReturn(List.of());

        // When
        List<Agent> result = agentQueryService.findByBranchCode(nonExistentBranchCode);

        // Then
        assertThat(result).isEmpty();

        verify(agentRepository).findByBranchCode(nonExistentBranchCode);
    }

    @Test
    @DisplayName("Should find agents by channel DIRECT")
    void shouldFindAgentsByChannelDirect() {
        // Given
        Channel channel = Channel.DIRECT;
        List<Agent> expectedAgents = Arrays.asList(testAgent1);

        when(agentRepository.findByChannel(channel)).thenReturn(expectedAgents);

        // When
        List<Agent> result = agentQueryService.findByChannel(channel);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testAgent1);
        assertThat(result.get(0).getChannel()).isEqualTo(Channel.DIRECT);

        verify(agentRepository).findByChannel(channel);
    }

    @Test
    @DisplayName("Should find agents by channel BROKER")
    void shouldFindAgentsByChannelBroker() {
        // Given
        Channel channel = Channel.BROKER;
        List<Agent> expectedAgents = Arrays.asList(testAgent2);

        when(agentRepository.findByChannel(channel)).thenReturn(expectedAgents);

        // When
        List<Agent> result = agentQueryService.findByChannel(channel);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testAgent2);
        assertThat(result.get(0).getChannel()).isEqualTo(Channel.BROKER);

        verify(agentRepository).findByChannel(channel);
    }

    @Test
    @DisplayName("Should return empty list when no agents found by channel")
    void shouldReturnEmptyListWhenNoAgentsFoundByChannel() {
        // Given
        Channel channel = Channel.BANCASSURANCE;

        when(agentRepository.findByChannel(channel)).thenReturn(List.of());

        // When
        List<Agent> result = agentQueryService.findByChannel(channel);

        // Then
        assertThat(result).isEmpty();

        verify(agentRepository).findByChannel(channel);
    }

    @Test
    @DisplayName("Should find all active agents")
    void shouldFindAllActiveAgents() {
        // Given
        List<Agent> expectedAgents = Arrays.asList(testAgent1, testAgent2);

        when(agentRepository.findAllActive()).thenReturn(expectedAgents);

        // When
        List<Agent> result = agentQueryService.findAllActive();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testAgent1, testAgent2);

        verify(agentRepository).findAllActive();
    }

    @Test
    @DisplayName("Should return empty list when no active agents found")
    void shouldReturnEmptyListWhenNoActiveAgentsFound() {
        // Given
        when(agentRepository.findAllActive()).thenReturn(List.of());

        // When
        List<Agent> result = agentQueryService.findAllActive();

        // Then
        assertThat(result).isEmpty();

        verify(agentRepository).findAllActive();
    }

    @Test
    @DisplayName("Should find all agents")
    void shouldFindAllAgents() {
        // Given
        List<Agent> expectedAgents = Arrays.asList(testAgent1, testAgent2);

        when(agentRepository.findAll()).thenReturn(expectedAgents);

        // When
        List<Agent> result = agentQueryService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testAgent1, testAgent2);

        verify(agentRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no agents found")
    void shouldReturnEmptyListWhenNoAgentsFound() {
        // Given
        when(agentRepository.findAll()).thenReturn(List.of());

        // When
        List<Agent> result = agentQueryService.findAll();

        // Then
        assertThat(result).isEmpty();

        verify(agentRepository).findAll();
    }

    @Test
    @DisplayName("Should not cache when agent not found")
    void shouldNotCacheWhenAgentNotFound() {
        // Given
        String agentCode = "AG99999";

        when(cachePort.getCachedAgentByCode(any(AgentCode.class))).thenReturn(Optional.empty());
        when(agentRepository.findByAgentCode(any(AgentCode.class))).thenReturn(Optional.empty());

        // When
        Optional<Agent> result = agentQueryService.findByAgentCode(agentCode);

        // Then
        assertThat(result).isEmpty();

        verify(cachePort, never()).cacheAgentByCode(any(AgentCode.class), any(Agent.class), anyInt());
    }

    @Test
    @DisplayName("Should handle multiple cache lookups efficiently")
    void shouldHandleMultipleCacheLookupsEfficiently() {
        // Given
        String agentCode1 = testAgentCode1.value();
        String agentCode2 = testAgentCode2.value();

        when(cachePort.getCachedAgentByCode(any(AgentCode.class)))
            .thenReturn(Optional.of(testAgent1))
            .thenReturn(Optional.of(testAgent2));

        // When
        Optional<Agent> result1 = agentQueryService.findByAgentCode(agentCode1);
        Optional<Agent> result2 = agentQueryService.findByAgentCode(agentCode2);

        // Then
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result1.get()).isEqualTo(testAgent1);
        assertThat(result2.get()).isEqualTo(testAgent2);

        verify(cachePort, times(2)).getCachedAgentByCode(any(AgentCode.class));
        verify(agentRepository, never()).findByAgentCode(any(AgentCode.class));
    }
}
