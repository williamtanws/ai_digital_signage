package io.jeecloud.aidigitalsignage.user.application.dto;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for User responses in REST API.
 */
@Schema(description = "User response containing all user details")
public record UserResponse(
    @Schema(description = "Unique user ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    String userId,
    
    @Schema(description = "User's new NRIC number", example = "990101121234")
    String newNric,
    
    @Schema(description = "User's email address", example = "john.doe@example.com")
    String email,
    
    @Schema(description = "User's full name", example = "John Doe")
    String name,
    
    @Schema(description = "Whether the user is currently active", example = "true")
    boolean status,
    
    @Schema(description = "Set of agent codes associated with this user", example = "[\"AG0001\", \"AG0002\"]")
    Set<String> associatedAgents,
    
    @Schema(description = "Timestamp when the user was created", example = "2026-01-15T10:30:00Z")
    Instant createDt,
    
    @Schema(description = "Timestamp when the user was last updated", example = "2026-01-15T15:45:00Z")
    Instant updateDt
) {
    /**
     * Factory method to create UserResponse from domain User.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId().getValue().toString(),
            user.getNewNric().getValue(),
            user.getEmail(),
            user.getName(),
            user.isStatus(),
            user.getAssociatedAgents().stream()
                .map(AgentCode::value)
                .collect(Collectors.toSet()),
            user.getCreateDt(),
            user.getUpdateDt()
        );
    }
}
