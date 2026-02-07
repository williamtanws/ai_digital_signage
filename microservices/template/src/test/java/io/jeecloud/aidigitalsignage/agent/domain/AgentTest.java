package io.jeecloud.aidigitalsignage.agent.domain;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Agent domain entity.
 * Tests business logic and domain rules.
 */
@DisplayName("Agent Domain Entity Tests")
class AgentTest {

    @Test
    @DisplayName("Should create a valid agent")
    void shouldCreateValidAgent() {
        // Given
        AgentCode code = AgentCode.of("AGT001");
        String name = "John Doe";
        String branchCode = "BR0001";
        Channel channel = Channel.DIRECT;

        // When
        Agent agent = Agent.create(code, name, branchCode, channel);

        // Then
        assertThat(agent).isNotNull();
        assertThat(agent.getId()).isNotNull();
        assertThat(agent.getAgentCode()).isEqualTo(code);
        assertThat(agent.getName()).isEqualTo(name);
        assertThat(agent.getBranchCode()).isEqualTo(branchCode);
        assertThat(agent.getChannel()).isEqualTo(channel);
        assertThat(agent.isStatus()).isTrue();
        assertThat(agent.getDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void shouldThrowExceptionWhenNameIsNull() {
        // Given
        AgentCode code = AgentCode.of("AGT001");
        String branchCode = "BR0001";
        Channel channel = Channel.DIRECT;

        // When & Then
        assertThatThrownBy(() -> Agent.create(code, null, branchCode, channel))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("name cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when name is too short")
    void shouldThrowExceptionWhenNameIsTooShort() {
        // Given
        AgentCode code = AgentCode.of("AGT001");
        String name = "J";
        String branchCode = "BR0001";
        Channel channel = Channel.DIRECT;

        // When & Then
        assertThatThrownBy(() -> Agent.create(code, name, branchCode, channel))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("must be between");
    }

    @Test
    @DisplayName("Should update agent successfully")
    void shouldUpdateAgentSuccessfully() {
        // Given
        Agent agent = Agent.create(
            AgentCode.of("AGT001"),
            "John Doe",
            "BR0001",
            Channel.DIRECT
        );
        agent.clearDomainEvents();

        String newName = "Jane Doe";
        String newBranchCode = "BR0002";
        Channel newChannel = Channel.BROKER;

        // When
        agent.update(newName, newBranchCode, newChannel);

        // Then
        assertThat(agent.getName()).isEqualTo(newName);
        assertThat(agent.getBranchCode()).isEqualTo(newBranchCode);
        assertThat(agent.getChannel()).isEqualTo(newChannel);
        assertThat(agent.getDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should deactivate agent")
    void shouldDeactivateAgent() {
        // Given
        Agent agent = Agent.create(
            AgentCode.of("AGT001"),
            "John Doe",
            "BR0001",
            Channel.DIRECT
        );

        // When
        agent.deactivate();

        // Then
        assertThat(agent.isStatus()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when deactivating inactive agent")
    void shouldThrowExceptionWhenDeactivatingInactiveAgent() {
        // Given
        Agent agent = Agent.create(
            AgentCode.of("AGT001"),
            "John Doe",
            "BR0001",
            Channel.DIRECT
        );
        agent.deactivate();

        // When & Then
        assertThatThrownBy(() -> agent.deactivate())
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("already inactive");
    }

    @Test
    @DisplayName("Should activate agent")
    void shouldActivateAgent() {
        // Given
        Agent agent = Agent.create(
            AgentCode.of("AGT001"),
            "John Doe",
            "BR0001",
            Channel.DIRECT
        );
        agent.deactivate();

        // When
        agent.activate();

        // Then
        assertThat(agent.isStatus()).isTrue();
    }

    @Test
    @DisplayName("Should check if agent can process transactions")
    void shouldCheckIfAgentCanProcessTransactions() {
        // Given
        Agent activeAgent = Agent.create(
            AgentCode.of("AGT001"),
            "John Doe",
            "BR0001",
            Channel.DIRECT
        );
        Agent inactiveAgent = Agent.create(
            AgentCode.of("AGT002"),
            "Jane Doe",
            "BR0001",
            Channel.DIRECT
        );
        inactiveAgent.deactivate();

        // When & Then
        assertThat(activeAgent.canProcessTransactions()).isTrue();
        assertThat(inactiveAgent.canProcessTransactions()).isFalse();
    }
}

