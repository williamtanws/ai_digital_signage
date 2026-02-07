package io.jeecloud.aidigitalsignage.agent.application.dto;

import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating/updating an agent via REST API.
 */
@Schema(description = "Request payload for creating or updating an agent")
public record AgentRequest(
    @Schema(
        description = "Unique agent code",
        example = "AGT001",
        minLength = 6,
        maxLength = 10
    )
    @NotBlank(message = "Agent code is required")
    @Size(min = 6, max = 10, message = "Agent code must be between 6 and 10 characters")
    String agentCode,
    
    @Schema(
        description = "Agent's full name",
        example = "John Doe",
        minLength = 2,
        maxLength = 100
    )
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,
    
    @Schema(
        description = "Branch code where the agent is assigned",
        example = "BR0001",
        minLength = 6,
        maxLength = 6
    )
    @NotBlank(message = "Branch code is required")
    @Size(min = 6, max = 6, message = "Branch code must be exactly 6 characters")
    String branchCode,
    
    @Schema(
        description = "Sales channel through which the agent operates",
        example = "AGENCY"
    )
    @NotNull(message = "Channel is required")
    Channel channel
) {}

