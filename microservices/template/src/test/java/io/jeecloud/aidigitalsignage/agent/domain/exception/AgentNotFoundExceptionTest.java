package io.jeecloud.aidigitalsignage.agent.domain.exception;

import io.jeecloud.aidigitalsignage.agent.domain.exception.AgentNotFoundException;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AgentNotFoundException Tests")
class AgentNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with agent code")
    void shouldCreateExceptionWithAgentCode() {
        // Given
        AgentCode agentCode = AgentCode.of("AGT001");

        // When
        AgentNotFoundException exception = new AgentNotFoundException(agentCode);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("AGT001");
    }

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        // Given
        String message = "Agent not found";

        // When
        AgentNotFoundException exception = new AgentNotFoundException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("Agent not found");
    }
}
