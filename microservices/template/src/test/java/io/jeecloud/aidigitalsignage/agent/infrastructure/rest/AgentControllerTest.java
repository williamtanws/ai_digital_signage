package io.jeecloud.aidigitalsignage.agent.infrastructure.rest;

import io.jeecloud.aidigitalsignage.agent.application.dto.AgentRequest;
import io.jeecloud.aidigitalsignage.agent.application.port.in.CreateAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.application.port.in.FindAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AgentController.
 * Tests REST API endpoints.
 */
@WebMvcTest(AgentController.class)
@DisplayName("AgentController Integration Tests")
@WithMockUser
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateAgentUseCase createAgentUseCase;

    @MockBean
    private io.jeecloud.aidigitalsignage.agent.application.port.in.UpdateAgentUseCase updateAgentUseCase;

    @MockBean
    private io.jeecloud.aidigitalsignage.agent.application.port.in.DeleteAgentUseCase deleteAgentUseCase;

    @MockBean
    private FindAgentUseCase findAgentUseCase;

    @Test
    @DisplayName("Should create agent via REST API")
    void shouldCreateAgentViaRestApi() throws Exception {
        // Given
        AgentRequest request = new AgentRequest(
            "AGT001",
            "John Doe",
            "BR0001",
            Channel.DIRECT
        );

        Agent mockAgent = Agent.create(
            AgentCode.of("AGT001"),
            "John Doe",
            "BR0001",
            Channel.DIRECT
        );

        when(createAgentUseCase.createAgent(any())).thenReturn(mockAgent);

        // When & Then
        mockMvc.perform(post("/api/v1/agents")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.agentCode").value("AGT001"))
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.branchCode").value("BR0001"))
            .andExpect(jsonPath("$.channel").value("DIRECT"));
    }

    @Test
    @DisplayName("Should return validation error for invalid request")
    void shouldReturnValidationErrorForInvalidRequest() throws Exception {
        // Given
        AgentRequest invalidRequest = new AgentRequest(
            "A", // Too short
            "J", // Too short
            "BR", // Too short
            Channel.DIRECT
        );

        // When & Then
        mockMvc.perform(post("/api/v1/agents")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 500 for deprecated endpoint - get agent by ID")
    void shouldReturn500ForDeprecatedGetAgentById() throws Exception {
        // Given
        UUID agentId = UUID.randomUUID();

        // When & Then - Deprecated endpoint throws UnsupportedOperationException
        mockMvc.perform(get("/api/v1/agents/" + agentId))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.type").value("https://api.allianz.com/problems/internal-error"));
    }

    @Test
    @DisplayName("Should return 500 for deprecated endpoint - agent not found")
    void shouldReturn500ForDeprecatedEndpointNotFound() throws Exception {
        // Given
        UUID agentId = UUID.randomUUID();

        // When & Then - Deprecated endpoint throws UnsupportedOperationException
        mockMvc.perform(get("/api/v1/agents/" + agentId))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.type").value("https://api.allianz.com/problems/internal-error"));
    }
}

