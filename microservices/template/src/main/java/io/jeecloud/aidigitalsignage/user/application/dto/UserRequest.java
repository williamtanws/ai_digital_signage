package io.jeecloud.aidigitalsignage.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating/updating a user via REST API.
 */
@Schema(description = "Request payload for creating or updating a user")
public record UserRequest(
    @Schema(
        description = "User's new NRIC number (12 digits)",
        example = "990101121234",
        minLength = 12,
        maxLength = 12
    )
    @NotBlank(message = "NRIC is required")
    @Size(min = 12, max = 12, message = "NRIC must be exactly 12 digits")
    String newNric,
    
    @Schema(
        description = "User's email address",
        example = "john.doe@example.com"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,
    
    @Schema(
        description = "User's name",
        example = "John Doe",
        minLength = 2,
        maxLength = 100
    )
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name
) {}
