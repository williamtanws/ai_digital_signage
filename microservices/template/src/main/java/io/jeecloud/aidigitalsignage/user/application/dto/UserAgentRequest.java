package io.jeecloud.aidigitalsignage.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for managing user-agent associations via REST API.
 */
@Schema(description = "Request payload for adding or removing agent associations")
public record UserAgentRequest(
    @Schema(
        description = "Agent code to associate with or remove from the user",
        example = "AG0001"
    )
    @NotBlank(message = "Agent code is required")
    String agentCode
) {}
