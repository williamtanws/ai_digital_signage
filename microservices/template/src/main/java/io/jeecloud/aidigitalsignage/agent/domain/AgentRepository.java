package io.jeecloud.aidigitalsignage.agent.domain;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;

import java.util.List;
import java.util.Optional;

/**
 * AgentRepository - Repository interface (Port) in Hexagonal Architecture.
 * This is part of the Domain layer and defines the contract for persistence.
 * Implementations will be in the Infrastructure layer.
 */
public interface AgentRepository {
    
    /**
     * Save a new agent or update an existing one.
     */
    Agent save(Agent agent);
    
    /**
     * Find an agent by its ID (AgentCode).
     */
    Optional<Agent> findById(AgentCode agentCode);
    
    /**
     * Find an agent by its business code.
     */
    Optional<Agent> findByAgentCode(AgentCode agentCode);
    
    /**
     * Find all agents by branch code.
     */
    List<Agent> findByBranchCode(String branchCode);
    
    /**
     * Find all agents by channel.
     */
    List<Agent> findByChannel(Channel channel);
    
    /**
     * Find all active agents.
     */
    List<Agent> findAllActive();
    
    /**
     * Find all inactive agents.
     */
    List<Agent> findAllInactive();
    
    /**
     * Find all agents by branch code and active status.
     */
    List<Agent> findByBranchCodeAndStatus(String branchCode, boolean status);
    
    /**
     * Find all agents by channel and active status.
     */
    List<Agent> findByChannelAndStatus(Channel channel, boolean status);
    
    /**
     * Find all agents.
     */
    List<Agent> findAll();
    
    /**
     * Delete an agent by ID (AgentCode).
     */
    void deleteById(AgentCode agentCode);
    
    /**
     * Check if an agent exists by code.
     */
    boolean existsByAgentCode(AgentCode agentCode);    
    /**
     * Find agents by dynamic filters.
     * Supports any combination of filters - null values are ignored.
     * 
     * @param branchCode Optional branch code filter
     * @param channel Optional channel filter
     * @param active Optional active status filter
     * @return List of agents matching all provided filters
     */
    List<Agent> findByFilters(String branchCode, Channel channel, Boolean active);}

