package io.jeecloud.aidigitalsignage.agent.application.port.in;

import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Input Port (Use Case) for querying Agents.
 */
public interface FindAgentUseCase {

    /**
     * Find an agent by agent code.
     */
    Optional<Agent> findByAgentCode(String agentCode);
    
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
     * Find all agents by channel and status.
     */
    List<Agent> findByChannelAndStatus(Channel channel, boolean status);
    
    /**
     * Find all agents.
     */
    List<Agent> findAll();
    
    /**
     * Find agents by dynamic filters.
     * Supports any combination of branchCode, channel, and active status.
     * All parameters are optional - null values are ignored in filtering.
     * 
     * @param branchCode Optional branch code filter
     * @param channel Optional channel filter
     * @param active Optional active status filter (true=active, false=inactive, null=all)
     * @return List of agents matching the filters
     */
    List<Agent> findByFilters(String branchCode, Channel channel, Boolean active);
}

