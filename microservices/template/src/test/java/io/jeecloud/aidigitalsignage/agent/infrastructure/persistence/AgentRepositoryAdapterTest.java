package io.jeecloud.aidigitalsignage.agent.infrastructure.persistence;

import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AgentRepositoryAdapter.
 * Tests repository adapter pattern bridging domain and persistence layers.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgentRepositoryAdapter Tests")
class AgentRepositoryAdapterTest {

    @Mock
    private AgentEntityManagerRepository agentEntityManagerRepository;

    @Mock
    private AgentJpaRepository agentJpaRepository;

    @InjectMocks
    private AgentRepositoryAdapter agentRepositoryAdapter;

    private Agent testAgent;
    private AgentEntity testEntity;
    private AgentCode testAgentCode;
    private String testBranchCode;

    @BeforeEach
    void setUp() {
        testAgentCode = AgentCode.of("AG00001");
        testBranchCode = "BR0001";
        
        testAgent = Agent.create(
            testAgentCode,
            "Test Agent",
            testBranchCode,
            Channel.DIRECT
        );
        
        testEntity = AgentEntity.builder()
            .agentCode(testAgentCode.value())
            .name("Test Agent")
            .branchCode(testBranchCode)
            .channel(Channel.DIRECT)
            .status(true)
            .createDt(Instant.now())
            .updateDt(Instant.now())
            .build();
    }

    @Test
    @DisplayName("Should save agent successfully using EntityManager repository")
    void shouldSaveAgentSuccessfully() {
        // Given
        when(agentEntityManagerRepository.save(any(AgentEntity.class))).thenReturn(testEntity);

        // When
        Agent savedAgent = agentRepositoryAdapter.save(testAgent);

        // Then
        assertThat(savedAgent).isNotNull();
        assertThat(savedAgent.getAgentCode()).isEqualTo(testAgentCode);
        assertThat(savedAgent.getName()).isEqualTo("Test Agent");
        assertThat(savedAgent.getBranchCode()).isEqualTo(testBranchCode);
        assertThat(savedAgent.getChannel()).isEqualTo(Channel.DIRECT);
        assertThat(savedAgent.isStatus()).isTrue();

        verify(agentEntityManagerRepository).save(any(AgentEntity.class));
    }

    @Test
    @DisplayName("Should find agent by ID using JPA repository")
    void shouldFindAgentById() {
        // Given
        when(agentJpaRepository.findById(testAgentCode.value())).thenReturn(Optional.of(testEntity));

        // When
        Optional<Agent> result = agentRepositoryAdapter.findById(testAgentCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAgentCode()).isEqualTo(testAgentCode);
        assertThat(result.get().getName()).isEqualTo("Test Agent");

        verify(agentJpaRepository).findById(testAgentCode.value());
    }

    @Test
    @DisplayName("Should return empty when agent not found by ID")
    void shouldReturnEmptyWhenAgentNotFoundById() {
        // Given
        AgentCode nonExistentCode = AgentCode.of("AG99999");
        when(agentJpaRepository.findById(nonExistentCode.value())).thenReturn(Optional.empty());

        // When
        Optional<Agent> result = agentRepositoryAdapter.findById(nonExistentCode);

        // Then
        assertThat(result).isEmpty();

        verify(agentJpaRepository).findById(nonExistentCode.value());
    }

    @Test
    @DisplayName("Should find agent by agent code using EntityManager repository")
    void shouldFindAgentByAgentCode() {
        // Given
        when(agentEntityManagerRepository.findByAgentCode(testAgentCode.value()))
            .thenReturn(Optional.of(testEntity));

        // When
        Optional<Agent> result = agentRepositoryAdapter.findByAgentCode(testAgentCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAgentCode()).isEqualTo(testAgentCode);

        verify(agentEntityManagerRepository).findByAgentCode(testAgentCode.value());
    }

    @Test
    @DisplayName("Should return empty when agent not found by agent code")
    void shouldReturnEmptyWhenAgentNotFoundByAgentCode() {
        // Given
        AgentCode nonExistentCode = AgentCode.of("AG99999");
        when(agentEntityManagerRepository.findByAgentCode(nonExistentCode.value()))
            .thenReturn(Optional.empty());

        // When
        Optional<Agent> result = agentRepositoryAdapter.findByAgentCode(nonExistentCode);

        // Then
        assertThat(result).isEmpty();

        verify(agentEntityManagerRepository).findByAgentCode(nonExistentCode.value());
    }

    @Test
    @DisplayName("Should find agents by branch code using JPA repository")
    void shouldFindAgentsByBranchCode() {
        // Given
        AgentEntity entity2 = AgentEntity.builder()
            .agentCode("AG00002")
            .name("Test Agent 2")
            .branchCode(testBranchCode)
            .channel(Channel.BROKER)
            .status(true)
            .createDt(Instant.now())
            .updateDt(Instant.now())
            .build();

        when(agentJpaRepository.findByBranchCode(testBranchCode))
            .thenReturn(Arrays.asList(testEntity, entity2));

        // When
        List<Agent> result = agentRepositoryAdapter.findByBranchCode(testBranchCode);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Agent::getBranchCode).containsOnly(testBranchCode);

        verify(agentJpaRepository).findByBranchCode(testBranchCode);
    }

    @Test
    @DisplayName("Should return empty list when no agents found by branch code")
    void shouldReturnEmptyListWhenNoAgentsByBranchCode() {
        // Given
        String nonExistentBranch = "BR999";
        when(agentJpaRepository.findByBranchCode(nonExistentBranch)).thenReturn(List.of());

        // When
        List<Agent> result = agentRepositoryAdapter.findByBranchCode(nonExistentBranch);

        // Then
        assertThat(result).isEmpty();

        verify(agentJpaRepository).findByBranchCode(nonExistentBranch);
    }

    @Test
    @DisplayName("Should find agents by channel using EntityManager repository")
    void shouldFindAgentsByChannel() {
        // Given
        AgentEntity entity2 = AgentEntity.builder()
            .agentCode("AG00002")
            .name("Test Agent 2")
            .branchCode("BR0002")
            .channel(Channel.BROKER)
            .status(true)
            .createDt(Instant.now())
            .updateDt(Instant.now())
            .build();

        when(agentEntityManagerRepository.findByChannel(Channel.BROKER))
            .thenReturn(Arrays.asList(entity2));

        // When
        List<Agent> result = agentRepositoryAdapter.findByChannel(Channel.BROKER);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).extracting(Agent::getChannel).containsOnly(Channel.BROKER);

        verify(agentEntityManagerRepository).findByChannel(Channel.BROKER);
    }

    @Test
    @DisplayName("Should return empty list when no agents found by channel")
    void shouldReturnEmptyListWhenNoAgentsByChannel() {
        // Given
        when(agentEntityManagerRepository.findByChannel(Channel.DIRECT)).thenReturn(List.of());

        // When
        List<Agent> result = agentRepositoryAdapter.findByChannel(Channel.DIRECT);

        // Then
        assertThat(result).isEmpty();

        verify(agentEntityManagerRepository).findByChannel(Channel.DIRECT);
    }

    @Test
    @DisplayName("Should find all active agents using JPA repository")
    void shouldFindAllActiveAgents() {
        // Given
        AgentEntity entity2 = AgentEntity.builder()
            .agentCode("AG00002")
            .name("Test Agent 2")
            .branchCode("BR0002")
            .channel(Channel.ONLINE)
            .status(true)
            .createDt(Instant.now())
            .updateDt(Instant.now())
            .build();

        when(agentJpaRepository.findAllActive()).thenReturn(Arrays.asList(testEntity, entity2));

        // When
        List<Agent> result = agentRepositoryAdapter.findAllActive();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Agent::isStatus);

        verify(agentJpaRepository).findAllActive();
    }

    @Test
    @DisplayName("Should return empty list when no active agents exist")
    void shouldReturnEmptyListWhenNoActiveAgents() {
        // Given
        when(agentJpaRepository.findAllActive()).thenReturn(List.of());

        // When
        List<Agent> result = agentRepositoryAdapter.findAllActive();

        // Then
        assertThat(result).isEmpty();

        verify(agentJpaRepository).findAllActive();
    }

    @Test
    @DisplayName("Should find all agents using EntityManager repository")
    void shouldFindAllAgents() {
        // Given
        AgentEntity inactiveEntity = AgentEntity.builder()
            .agentCode("AG00003")
            .name("Inactive Agent")
            .branchCode("BR0003")
            .channel(Channel.BROKER)
            .status(false)
            .createDt(Instant.now())
            .updateDt(Instant.now())
            .build();

        when(agentEntityManagerRepository.findAll())
            .thenReturn(Arrays.asList(testEntity, inactiveEntity));

        // When
        List<Agent> result = agentRepositoryAdapter.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Agent::isStatus).contains(true, false);

        verify(agentEntityManagerRepository).findAll();
    }

    @Test
    @DisplayName("Should delete agent by ID using JPA repository")
    void shouldDeleteAgentById() {
        // Given
        doNothing().when(agentJpaRepository).deleteById(testAgentCode.value());

        // When
        agentRepositoryAdapter.deleteById(testAgentCode);

        // Then
        verify(agentJpaRepository).deleteById(testAgentCode.value());
    }

    @Test
    @DisplayName("Should check if agent exists by agent code using EntityManager repository")
    void shouldCheckIfAgentExistsByAgentCode() {
        // Given
        when(agentEntityManagerRepository.existsByAgentCode(testAgentCode.value())).thenReturn(true);

        // When
        boolean exists = agentRepositoryAdapter.existsByAgentCode(testAgentCode);

        // Then
        assertThat(exists).isTrue();

        verify(agentEntityManagerRepository).existsByAgentCode(testAgentCode.value());
    }

    @Test
    @DisplayName("Should return false when agent does not exist")
    void shouldReturnFalseWhenAgentDoesNotExist() {
        // Given
        AgentCode nonExistentCode = AgentCode.of("AG99999");
        when(agentEntityManagerRepository.existsByAgentCode(nonExistentCode.value())).thenReturn(false);

        // When
        boolean exists = agentRepositoryAdapter.existsByAgentCode(nonExistentCode);

        // Then
        assertThat(exists).isFalse();

        verify(agentEntityManagerRepository).existsByAgentCode(nonExistentCode.value());
    }

    @Test
    @DisplayName("Should correctly map domain agent to entity")
    void shouldCorrectlyMapDomainToEntity() {
        // When
        when(agentEntityManagerRepository.save(any(AgentEntity.class))).thenAnswer(invocation -> {
            AgentEntity capturedEntity = invocation.getArgument(0);
            assertThat(capturedEntity.getAgentCode()).isEqualTo(testAgentCode.value());
            assertThat(capturedEntity.getName()).isEqualTo("Test Agent");
            assertThat(capturedEntity.getBranchCode()).isEqualTo(testBranchCode);
            assertThat(capturedEntity.getChannel()).isEqualTo(Channel.DIRECT);
            assertThat(capturedEntity.isStatus()).isTrue();
            return testEntity;
        });

        // Then
        agentRepositoryAdapter.save(testAgent);

        verify(agentEntityManagerRepository).save(any(AgentEntity.class));
    }

    @Test
    @DisplayName("Should correctly map entity to domain agent")
    void shouldCorrectlyMapEntityToDomain() {
        // Given
        when(agentJpaRepository.findById(testAgentCode.value())).thenReturn(Optional.of(testEntity));

        // When
        Optional<Agent> result = agentRepositoryAdapter.findById(testAgentCode);

        // Then
        assertThat(result).isPresent();
        Agent agent = result.get();
        assertThat(agent.getAgentCode().value()).isEqualTo(testEntity.getAgentCode());
        assertThat(agent.getName()).isEqualTo(testEntity.getName());
        assertThat(agent.getBranchCode()).isEqualTo(testEntity.getBranchCode());
        assertThat(agent.getChannel()).isEqualTo(testEntity.getChannel());
        assertThat(agent.isStatus()).isEqualTo(testEntity.isStatus());
    }
}
