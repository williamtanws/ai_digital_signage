package io.jeecloud.aidigitalsignage.agent.infrastructure.rest;

import io.jeecloud.aidigitalsignage.agent.application.dto.AgentRequest;
import io.jeecloud.aidigitalsignage.agent.application.dto.AgentResponse;
import io.jeecloud.aidigitalsignage.agent.application.port.in.CreateAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.application.port.in.DeleteAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.application.port.in.FindAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.application.port.in.UpdateAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import io.jeecloud.aidigitalsignage.agent.domain.exception.AgentNotFoundException;
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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Agent operations.
 * Primary/Driving Adapter in Hexagonal Architecture.
 * Exposes HTTP endpoints and delegates to use cases.
 */
@RestController
@RequestMapping("/api/v1/agents")
@Tag(name = "Agent Management", description = "APIs for managing insurance agents, including creation, updates, queries, and deletion")
public class AgentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);
    
    private final CreateAgentUseCase createAgentUseCase;
    private final UpdateAgentUseCase updateAgentUseCase;
    private final DeleteAgentUseCase deleteAgentUseCase;
    private final FindAgentUseCase findAgentUseCase;

    public AgentController(
        CreateAgentUseCase createAgentUseCase,
        UpdateAgentUseCase updateAgentUseCase,
        DeleteAgentUseCase deleteAgentUseCase,
        FindAgentUseCase findAgentUseCase
    ) {
        this.createAgentUseCase = createAgentUseCase;
        this.updateAgentUseCase = updateAgentUseCase;
        this.deleteAgentUseCase = deleteAgentUseCase;
        this.findAgentUseCase = findAgentUseCase;
    }

    /**
     * Create a new agent.
     */
    @Operation(
        summary = "Create a new agent",
        description = "Creates a new insurance agent with the provided details. Agent code must be unique."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Agent created successfully",
            content = @Content(schema = @Schema(implementation = AgentResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Agent with the same code already exists",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<AgentResponse> createAgent(
        @Parameter(description = "Agent details to create", required = true)
        @Valid @RequestBody AgentRequest request
    ) {
        logger.info("REST: Creating agent with code: {}", request.agentCode());
        
        CreateAgentUseCase.CreateAgentCommand command = 
            new CreateAgentUseCase.CreateAgentCommand(
                request.agentCode(),
                request.name(),
                request.branchCode(),
                request.channel()
            );
        
        Agent agent = createAgentUseCase.createAgent(command);
        AgentResponse response = AgentResponse.from(agent);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get agent by agent code.
     */
    @Operation(
        summary = "Get agent by code",
        description = "Retrieves an agent by their unique agent code"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Agent found",
            content = @Content(schema = @Schema(implementation = AgentResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Agent not found",
            content = @Content
        )
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<AgentResponse> getAgentByCode(
        @Parameter(description = "Agent code (6-10 characters)", required = true, example = "AGT001")
        @PathVariable String code
    ) {
        logger.debug("REST: Getting agent by code: {}", code);
        
        return findAgentUseCase.findByAgentCode(code)
            .map(AgentResponse::from)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new AgentNotFoundException(code));
    }

    /**
     * Get all agents.
     */
    @Operation(
        summary = "Get all agents",
        description = "Retrieves all agents with optional filtering by branch code, channel, or active status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of agents retrieved successfully",
            content = @Content(schema = @Schema(implementation = AgentResponse.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<AgentResponse>> getAllAgents(
        @Parameter(description = "Filter by branch code (6 characters)", example = "BR0001")
        @RequestParam(required = false) String branchCode,
        @Parameter(description = "Filter by sales channel")
        @RequestParam(required = false) Channel channel,
        @Parameter(description = "Filter by active status: true for active only, false for inactive only, omit for all")
        @RequestParam(required = false) Boolean active
    ) {
        logger.debug("REST: Getting all agents (branchCode={}, channel={}, active={})", 
            branchCode, channel, active);
        
        // Use dynamic filtering - handles any combination of filters
        List<Agent> agents = findAgentUseCase.findByFilters(branchCode, channel, active);
        
        List<AgentResponse> responses = agents.stream()
            .map(AgentResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Update an existing agent.
     */
    @Operation(
        summary = "Update an existing agent",
        description = "Updates an existing agent's details. Agent code cannot be changed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Agent updated successfully",
            content = @Content(schema = @Schema(implementation = AgentResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Agent not found",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<AgentResponse> updateAgent(
        @Parameter(description = "Agent code (primary key)", required = true)
        @PathVariable String id,
        @Parameter(description = "Updated agent details", required = true)
        @Valid @RequestBody AgentRequest request
    ) {
        logger.info("REST: Updating agent with code: {}", id);
        
        UpdateAgentUseCase.UpdateAgentCommand command = 
            new UpdateAgentUseCase.UpdateAgentCommand(
                id,
                request.name(),
                request.branchCode(),
                request.channel()
            );
        
        Agent agent = updateAgentUseCase.updateAgent(command);
        AgentResponse response = AgentResponse.from(agent);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Change agent active status.
     */
    @Operation(
        summary = "Change agent active status",
        description = "Activates or deactivates an agent by their agent code. Changes are cached in Redis and published via Kafka."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Agent active status changed successfully",
            content = @Content(schema = @Schema(implementation = AgentResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Agent not found",
            content = @Content
        )
    })
    @PatchMapping("/active/{agent}/{active}")
    public ResponseEntity<AgentResponse> changeAgentActive(
        @Parameter(description = "Agent code (6-10 characters)", required = true, example = "AGT001")
        @PathVariable String agent,
        @Parameter(description = "New active status (true = activate, false = deactivate)", required = true)
        @PathVariable Boolean active
    ) {
        logger.info("REST: Changing agent active status for code: {} to {}", agent, active);
        
        // Use update use case to change active status
        UpdateAgentUseCase.ChangeActiveStatusCommand command = 
            new UpdateAgentUseCase.ChangeActiveStatusCommand(agent, active);
        
        Agent updatedAgent = updateAgentUseCase.changeActiveStatus(command);
        
        AgentResponse response = AgentResponse.from(updatedAgent);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an agent.
     */
    @Operation(
        summary = "Delete an agent",
        description = "Soft deletes an agent by setting their active status to false"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Agent deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Agent not found",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(
        @Parameter(description = "Agent unique identifier", required = true)
        @PathVariable UUID id
    ) {
        logger.info("REST: Deleting agent with ID: {}", id);
        
        deleteAgentUseCase.deleteAgent(id);
        
        return ResponseEntity.noContent().build();
    }
}

