package io.jeecloud.aidigitalsignage.agent.application.service;

import io.jeecloud.aidigitalsignage.agent.application.port.in.CreateAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.application.port.in.UpdateAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.application.port.out.AgentCachePort;
import io.jeecloud.aidigitalsignage.agent.application.port.out.AgentEventPublisher;
import io.jeecloud.aidigitalsignage.agent.domain.*;
import io.jeecloud.aidigitalsignage.agent.domain.exception.AgentNotFoundException;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AgentCommandService.
 * Tests command side of CQRS (Create, Update, Delete).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgentCommandService Tests")
class AgentCommandServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AgentEventPublisher eventPublisher;

    @Mock
    private AgentCachePort cachePort;

    @InjectMocks
    private AgentCommandService commandService;

    private Agent testAgent;
    private AgentCode testAgentCode;
    private String testBranchCode;
    private String testName;

    @BeforeEach
    void setUp() {
        testAgentCode = AgentCode.of("AG00001");
        testBranchCode = "BR0001";
        testName = "Test Agent";
        
        testAgent = Agent.create(
            testAgentCode,
            testName,
            testBranchCode,
            Channel.DIRECT
        );
    }

    @Test
    @DisplayName("Should create agent successfully with valid data")
    void shouldCreateAgentSuccessfully() {
        // Given
        CreateAgentUseCase.CreateAgentCommand command = new CreateAgentUseCase.CreateAgentCommand(
            "AG00001",
            "Test Agent",
            "BR0001",
            Channel.DIRECT
        );

        when(agentRepository.existsByAgentCode(any(AgentCode.class))).thenReturn(false);
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Agent createdAgent = commandService.createAgent(command);

        // Then
        assertThat(createdAgent).isNotNull();
        assertThat(createdAgent.getName()).isEqualTo("Test Agent");
        assertThat(createdAgent.getBranchCode()).isEqualTo("BR0001");
        assertThat(createdAgent.getChannel()).isEqualTo(Channel.DIRECT);
        
        verify(agentRepository).existsByAgentCode(any(AgentCode.class));
        verify(agentRepository).save(any(Agent.class));
        verify(eventPublisher, atLeastOnce()).publishAgentCreated(any());
    }

    @Test
    @DisplayName("Should throw exception when agent code already exists")
    void shouldThrowExceptionWhenAgentCodeAlreadyExists() {
        // Given
        CreateAgentUseCase.CreateAgentCommand command = new CreateAgentUseCase.CreateAgentCommand(
            "AG00001",
            "Test Agent",
            "BR0001",
            Channel.DIRECT
        );

        when(agentRepository.existsByAgentCode(any(AgentCode.class))).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> commandService.createAgent(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("Agent with code AG00001 already exists");

        verify(agentRepository).existsByAgentCode(any(AgentCode.class));
        verify(agentRepository, never()).save(any(Agent.class));
        verify(eventPublisher, never()).publishAgentCreated(any());
    }

    @Test
    @DisplayName("Should create agent with different channels")
    void shouldCreateAgentWithDifferentChannels() {
        // Given
        CreateAgentUseCase.CreateAgentCommand command = new CreateAgentUseCase.CreateAgentCommand(
            "AG00002",
            "Agency Agent",
            "BR0002",
            Channel.BROKER
        );

        when(agentRepository.existsByAgentCode(any(AgentCode.class))).thenReturn(false);
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Agent createdAgent = commandService.createAgent(command);

        // Then
        assertThat(createdAgent).isNotNull();
        assertThat(createdAgent.getChannel()).isEqualTo(Channel.BROKER);
        
        verify(agentRepository).existsByAgentCode(any(AgentCode.class));
        verify(agentRepository).save(any(Agent.class));
    }

    @Test
    @DisplayName("Should update agent successfully")
    void shouldUpdateAgentSuccessfully() {
        // Given
        String updatedName = "Updated Agent Name";
        String updatedBranchCode = "BR0002";
        Channel updatedChannel = Channel.BANCASSURANCE;

        UpdateAgentUseCase.UpdateAgentCommand command = new UpdateAgentUseCase.UpdateAgentCommand(
            "AG00001",
            updatedName,
            updatedBranchCode,
            updatedChannel
        );

        when(agentRepository.findById(any(AgentCode.class))).thenReturn(Optional.of(testAgent));
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Agent updatedAgent = commandService.updateAgent(command);

        // Then
        assertThat(updatedAgent).isNotNull();
        assertThat(updatedAgent.getName()).isEqualTo(updatedName);
        assertThat(updatedAgent.getBranchCode()).isEqualTo(updatedBranchCode);
        assertThat(updatedAgent.getChannel()).isEqualTo(updatedChannel);

        verify(agentRepository).findById(any(AgentCode.class));
        verify(agentRepository).save(any(Agent.class));
        verify(cachePort).invalidateAgent(any(AgentCode.class));
        verify(eventPublisher, atLeastOnce()).publishAgentCreated(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent agent")
    void shouldThrowExceptionWhenUpdatingNonExistentAgent() {
        // Given
        UpdateAgentUseCase.UpdateAgentCommand command = new UpdateAgentUseCase.UpdateAgentCommand(
            "AG99999",
            "Updated Name",
            "BR0002",
            Channel.DIRECT
        );

        when(agentRepository.findById(any(AgentCode.class))).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> commandService.updateAgent(command))
            .isInstanceOf(AgentNotFoundException.class);

        verify(agentRepository).findById(any(AgentCode.class));
        verify(agentRepository, never()).save(any(Agent.class));
        verify(cachePort, never()).invalidateAgent(any(AgentCode.class));
        verify(eventPublisher, never()).publishAgentCreated(any());
    }

    @Test
    @DisplayName("Should invalidate cache after update")
    void shouldInvalidateCacheAfterUpdate() {
        // Given
        UpdateAgentUseCase.UpdateAgentCommand command = new UpdateAgentUseCase.UpdateAgentCommand(
            "AG00001",
            "Updated Name",
            "BR0002",
            Channel.DIRECT
        );

        when(agentRepository.findById(any(AgentCode.class))).thenReturn(Optional.of(testAgent));
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        commandService.updateAgent(command);

        // Then
        verify(cachePort).invalidateAgent(any(AgentCode.class));
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when deleting by UUID")
    void shouldThrowExceptionWhenDeletingByUUID() {
        // Given
        UUID agentId = UUID.randomUUID();

        // When / Then
        assertThatThrownBy(() -> commandService.deleteAgent(agentId))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Please use deleteAgentByCode instead");

        verify(agentRepository, never()).deleteById(any(AgentCode.class));
        verify(cachePort, never()).invalidateAgent(any(AgentCode.class));
    }

    @Test
    @DisplayName("Should delete agent by code successfully")
    void shouldDeleteAgentByCodeSuccessfully() {
        // Given
        String agentCode = "AG00001";

        when(agentRepository.findById(any(AgentCode.class))).thenReturn(Optional.of(testAgent));
        doNothing().when(agentRepository).deleteById(any(AgentCode.class));

        // When
        commandService.deleteAgentByCode(agentCode);

        // Then
        verify(agentRepository).findById(any(AgentCode.class));
        verify(agentRepository).deleteById(any(AgentCode.class));
        verify(cachePort).invalidateAgent(any(AgentCode.class));
        verify(eventPublisher, atLeastOnce()).publishAgentCreated(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent agent")
    void shouldThrowExceptionWhenDeletingNonExistentAgent() {
        // Given
        String agentCode = "AG99999";

        when(agentRepository.findById(any(AgentCode.class))).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> commandService.deleteAgentByCode(agentCode))
            .isInstanceOf(AgentNotFoundException.class);

        verify(agentRepository).findById(any(AgentCode.class));
        verify(agentRepository, never()).deleteById(any(AgentCode.class));
        verify(cachePort, never()).invalidateAgent(any(AgentCode.class));
    }

    @Test
    @DisplayName("Should handle event publishing failure gracefully")
    void shouldHandleEventPublishingFailure() {
        // Given
        CreateAgentUseCase.CreateAgentCommand command = new CreateAgentUseCase.CreateAgentCommand(
            "AG00001",
            "Test Agent",
            "BR0001",
            Channel.DIRECT
        );

        when(agentRepository.existsByAgentCode(any(AgentCode.class))).thenReturn(false);
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Failed to publish event")).when(eventPublisher).publishAgentCreated(any());

        // When / Then
        assertThatThrownBy(() -> commandService.createAgent(command))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to publish domain event");

        verify(agentRepository).existsByAgentCode(any(AgentCode.class));
        verify(agentRepository).save(any(Agent.class));
        verify(eventPublisher).publishAgentCreated(any());
    }

    @Test
    @DisplayName("Should publish events and clear domain events after create")
    void shouldPublishEventsAndClearAfterCreate() {
        // Given
        CreateAgentUseCase.CreateAgentCommand command = new CreateAgentUseCase.CreateAgentCommand(
            "AG00001",
            "Test Agent",
            "BR0001",
            Channel.DIRECT
        );

        when(agentRepository.existsByAgentCode(any(AgentCode.class))).thenReturn(false);
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Agent createdAgent = commandService.createAgent(command);

        // Then
        assertThat(createdAgent.getDomainEvents()).isEmpty();
        verify(eventPublisher, atLeastOnce()).publishAgentCreated(any());
    }

    @Test
    @DisplayName("Should publish events and clear domain events after update")
    void shouldPublishEventsAndClearAfterUpdate() {
        // Given
        UpdateAgentUseCase.UpdateAgentCommand command = new UpdateAgentUseCase.UpdateAgentCommand(
            "AG00001",
            "Updated Name",
            "BR0002",
            Channel.DIRECT
        );

        when(agentRepository.findById(any(AgentCode.class))).thenReturn(Optional.of(testAgent));
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Agent updatedAgent = commandService.updateAgent(command);

        // Then
        assertThat(updatedAgent.getDomainEvents()).isEmpty();
        verify(eventPublisher, atLeastOnce()).publishAgentCreated(any());
    }

    @Test
    @DisplayName("Should publish events after delete")
    void shouldPublishEventsAfterDelete() {
        // Given
        String agentCode = "AG00001";

        when(agentRepository.findById(any(AgentCode.class))).thenReturn(Optional.of(testAgent));
        doNothing().when(agentRepository).deleteById(any(AgentCode.class));

        // When
        commandService.deleteAgentByCode(agentCode);

        // Then
        verify(eventPublisher, atLeastOnce()).publishAgentCreated(any());
    }
}

