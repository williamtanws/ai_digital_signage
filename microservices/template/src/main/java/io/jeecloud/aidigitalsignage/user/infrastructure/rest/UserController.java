package io.jeecloud.aidigitalsignage.user.infrastructure.rest;

import io.jeecloud.aidigitalsignage.user.application.dto.UserRequest;
import io.jeecloud.aidigitalsignage.user.application.dto.UserResponse;
import io.jeecloud.aidigitalsignage.user.application.port.in.CreateUserUseCase;
import io.jeecloud.aidigitalsignage.user.application.port.in.DeleteUserUseCase;
import io.jeecloud.aidigitalsignage.user.application.port.in.FindUserUseCase;
import io.jeecloud.aidigitalsignage.user.application.port.in.UpdateUserUseCase;
import io.jeecloud.aidigitalsignage.user.domain.User;
import io.jeecloud.aidigitalsignage.user.domain.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for User operations.
 * Primary/Driving Adapter in Hexagonal Architecture.
 * Exposes HTTP endpoints and delegates to use cases.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "APIs for managing users, including creation, updates, queries, and deletion")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final FindUserUseCase findUserUseCase;

    public UserController(
        CreateUserUseCase createUserUseCase,
        UpdateUserUseCase updateUserUseCase,
        DeleteUserUseCase deleteUserUseCase,
        FindUserUseCase findUserUseCase
    ) {
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.findUserUseCase = findUserUseCase;
    }

    /**
     * Create a new user.
     */
    @Operation(
        summary = "Create a new user",
        description = "Creates a new user with the provided details. NRIC must be unique."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "User with the same NRIC already exists",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
        @Parameter(description = "User details to create", required = true)
        @Valid @RequestBody UserRequest request
    ) {
        logger.info("REST: Creating user with NRIC: {}", request.newNric());
        
        CreateUserUseCase.CreateUserCommand command = 
            new CreateUserUseCase.CreateUserCommand(
                request.newNric(),
                request.email(),
                request.name()
            );
        
        User user = createUserUseCase.createUser(command);
        UserResponse response = UserResponse.from(user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get user by ID.
     */
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a user by their unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
        @Parameter(description = "User unique identifier", required = true, example = "alex.wong")
        @PathVariable String id
    ) {
        logger.debug("REST: Getting user by ID: {}", id);
        
        FindUserUseCase.FindByIdQuery query = new FindUserUseCase.FindByIdQuery(id);
        
        return findUserUseCase.findById(query)
            .map(UserResponse::from)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Get user by NRIC.
     */
    @Operation(
        summary = "Get user by NRIC",
        description = "Retrieves a user by their NRIC number"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        )
    })
    @GetMapping("/nric/{nric}")
    public ResponseEntity<UserResponse> getUserByNric(
        @Parameter(description = "User NRIC (12 digits)", required = true, example = "990101121234")
        @PathVariable String nric
    ) {
        logger.debug("REST: Getting user by NRIC: {}", nric);
        
        FindUserUseCase.FindByNricQuery query = new FindUserUseCase.FindByNricQuery(nric);
        
        return findUserUseCase.findByNric(query)
            .map(UserResponse::from)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new UserNotFoundException("NRIC: " + nric));
    }

    /**
     * Get all active users.
     */
    @Operation(
        summary = "Get all active users",
        description = "Retrieves all users with active status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of active users retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllActiveUsers() {
        logger.debug("REST: Getting all active users");
        
        FindUserUseCase.FindAllActiveQuery query = new FindUserUseCase.FindAllActiveQuery();
        List<User> users = findUserUseCase.findAllActive(query);
        
        List<UserResponse> responses = users.stream()
            .map(UserResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Update an existing user.
     */
    @Operation(
        summary = "Update an existing user",
        description = "Updates an existing user's details. NRIC cannot be changed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
        @Parameter(description = "User unique identifier", required = true, example = "alex.wong")
        @PathVariable String id,
        @Parameter(description = "Updated user details", required = true)
        @Valid @RequestBody UserRequest request
    ) {
        logger.info("REST: Updating user with ID: {}", id);
        
        UpdateUserUseCase.UpdateUserCommand command = 
            new UpdateUserUseCase.UpdateUserCommand(
                id,
                request.email(),
                request.name()
            );
        
        User user = updateUserUseCase.updateUser(command);
        UserResponse response = UserResponse.from(user);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a user.
     */
    @Operation(
        summary = "Delete a user",
        description = "Soft deletes a user by setting their active status to false"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "User unique identifier", required = true, example = "alex.wong")
        @PathVariable String id
    ) {
        logger.info("REST: Deleting user with ID: {}", id);
        
        DeleteUserUseCase.DeleteUserCommand command = new DeleteUserUseCase.DeleteUserCommand(id);
        deleteUserUseCase.deleteUser(command);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Change user active status (activate/deactivate).
     */
    @Operation(
        summary = "Change user active status",
        description = "Activates or deactivates a user. When a user is deactivated, all associated agents will also be deactivated via Kafka events."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User active status changed successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        )
    })
    @PatchMapping("/active/{userId}/{active}")
    public ResponseEntity<UserResponse> changeUserActive(
        @Parameter(description = "User unique identifier", required = true, example = "alex.wong")
        @PathVariable String userId,
        @Parameter(description = "Active status (true=active, false=inactive)", required = true)
        @PathVariable Boolean active
    ) {
        logger.info("REST: Changing user active status for ID: {} to {}", userId, active);
        
        UpdateUserUseCase.ChangeActiveStatusCommand command = 
            new UpdateUserUseCase.ChangeActiveStatusCommand(userId, active);
        
        User user = updateUserUseCase.changeActiveStatus(command);
        UserResponse response = UserResponse.from(user);
        
        return ResponseEntity.ok(response);
    }
}
