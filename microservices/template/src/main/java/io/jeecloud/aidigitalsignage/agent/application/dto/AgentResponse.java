package io.jeecloud.aidigitalsignage.agent.application.dto;

import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO for Agent responses in REST API.
 */
@Schema(description = "Agent response containing all agent details")
public record AgentResponse(
    @Schema(description = "Unique agent code (primary key)", example = "AGT001")
    String agentCode,
    
    @Schema(description = "Agent's full name", example = "John Doe")
    String name,
    
    @Schema(description = "Branch code where the agent is assigned", example = "BR0001")
    String branchCode,
    
    @Schema(description = "Sales channel through which the agent operates", example = "AGENCY")
    Channel channel,
    
    @Schema(description = "Whether the agent is currently active", example = "true")
    boolean status,
    
    @Schema(description = "Timestamp when the agent was created", example = "2026-01-12T10:30:00Z")
    Instant createDt,
    
    @Schema(description = "Timestamp when the agent was last updated", example = "2026-01-12T15:45:00Z")
    Instant updateDt
) {
    /**
     * Factory method to create AgentResponse from domain Agent.
     */
    public static AgentResponse from(Agent agent) {
        return new AgentResponse(
            agent.getAgentCode().value(),
            agent.getName(),
            agent.getBranchCode(),
            agent.getChannel(),
            agent.isStatus(),
            agent.getCreateDt(),
            agent.getUpdateDt()
        );
    }
}

