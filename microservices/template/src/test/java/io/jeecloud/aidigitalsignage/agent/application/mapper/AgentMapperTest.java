package io.jeecloud.aidigitalsignage.agent.application.mapper;

import io.jeecloud.aidigitalsignage.agent.application.dto.AgentResponse;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AgentMapper.
 * Tests MapStruct mapper for Agent DTO conversions.
 */
@DisplayName("AgentMapper Tests")
class AgentMapperTest {

    private AgentMapper agentMapper;

    @BeforeEach
    void setUp() {
        agentMapper = Mappers.getMapper(AgentMapper.class);
    }

    @Test
    @DisplayName("Should map agent to response successfully")
    void shouldMapAgentToResponse() {
        // Given
        AgentCode agentCode = AgentCode.of("AG00001");
        String name = "Test Agent";
        String branchCode = "BR0001";
        Channel channel = Channel.DIRECT;

        Agent agent = Agent.create(agentCode, name, branchCode, channel);

        // When
        AgentResponse response = agentMapper.toResponse(agent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.agentCode()).isEqualTo("AG00001");
        assertThat(response.name()).isEqualTo(name);
        assertThat(response.branchCode()).isEqualTo(branchCode);
        assertThat(response.channel()).isEqualTo(channel);
        assertThat(response.status()).isTrue();
        assertThat(response.createDt()).isNotNull();
        assertThat(response.updateDt()).isNotNull();
    }

    @Test
    @DisplayName("Should map inactive agent correctly")
    void shouldMapInactiveAgent() {
        // Given
        AgentCode agentCode = AgentCode.of("AG00002");
        Agent agent = Agent.create(agentCode, "Inactive Agent", "BR0002", Channel.DIRECT);
        agent.deactivate();

        // When
        AgentResponse response = agentMapper.toResponse(agent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.agentCode()).isEqualTo("AG00002");
        assertThat(response.status()).isFalse();
    }

    @Test
    @DisplayName("Should map agent with all channel types")
    void shouldMapAgentWithDifferentChannels() {
        // Test DIRECT channel
        Agent directAgent = Agent.create(AgentCode.of("AG00001"), "Direct Agent", "BR0001", Channel.DIRECT);
        AgentResponse directResponse = agentMapper.toResponse(directAgent);
        assertThat(directResponse.channel()).isEqualTo(Channel.DIRECT);

        // Test BROKER channel
        Agent brokerAgent = Agent.create(AgentCode.of("AG00002"), "Broker Agent", "BR0002", Channel.BROKER);
        AgentResponse brokerResponse = agentMapper.toResponse(brokerAgent);
        assertThat(brokerResponse.channel()).isEqualTo(Channel.BROKER);

        // Test BANCASSURANCE channel
        Agent bancassuranceAgent = Agent.create(AgentCode.of("AG00003"), "Bancassurance Agent", "BR0003", Channel.BANCASSURANCE);
        AgentResponse bancassuranceResponse = agentMapper.toResponse(bancassuranceAgent);
        assertThat(bancassuranceResponse.channel()).isEqualTo(Channel.BANCASSURANCE);

        // Test ONLINE channel
        Agent onlineAgent = Agent.create(AgentCode.of("AG00004"), "Online Agent", "BR0004", Channel.ONLINE);
        AgentResponse onlineResponse = agentMapper.toResponse(onlineAgent);
        assertThat(onlineResponse.channel()).isEqualTo(Channel.ONLINE);
    }

    @Test
    @DisplayName("Should preserve timestamps in mapping")
    void shouldPreserveTimestampsInMapping() {
        // Given
        Agent agent = Agent.create(AgentCode.of("AG00001"), "Test Agent", "BR0001", Channel.DIRECT);

        // When
        AgentResponse response = agentMapper.toResponse(agent);

        // Then
        assertThat(response.createDt()).isEqualTo(agent.getCreateDt());
        assertThat(response.updateDt()).isEqualTo(agent.getUpdateDt());
    }

    @Test
    @DisplayName("Should handle agent code value object correctly")
    void shouldHandleAgentCodeValueObject() {
        // Given
        AgentCode agentCode = AgentCode.of("AG123456");
        Agent agent = Agent.create(agentCode, "Test Agent", "BR0001", Channel.DIRECT);

        // When
        AgentResponse response = agentMapper.toResponse(agent);

        // Then
        assertThat(response.agentCode()).isEqualTo(agentCode.value());
        assertThat(response.agentCode()).isEqualTo("AG123456");
    }

    @Test
    @DisplayName("Should map multiple agents independently")
    void shouldMapMultipleAgentsIndependently() {
        // Given
        Agent agent1 = Agent.create(AgentCode.of("AG00001"), "Agent 1", "BR0001", Channel.BROKER);
        Agent agent2 = Agent.create(AgentCode.of("AG00002"), "Agent 2", "BR0002", Channel.DIRECT);

        // When
        AgentResponse response1 = agentMapper.toResponse(agent1);
        AgentResponse response2 = agentMapper.toResponse(agent2);

        // Then
        assertThat(response1.agentCode()).isEqualTo("AG00001");
        assertThat(response1.name()).isEqualTo("Agent 1");
        assertThat(response1.branchCode()).isEqualTo("BR0001");
        assertThat(response1.channel()).isEqualTo(Channel.BROKER);

        assertThat(response2.agentCode()).isEqualTo("AG00002");
        assertThat(response2.name()).isEqualTo("Agent 2");
        assertThat(response2.branchCode()).isEqualTo("BR0002");
        assertThat(response2.channel()).isEqualTo(Channel.DIRECT);
    }
}
