package io.jeecloud.aidigitalsignage.agent.application.service;

import io.jeecloud.aidigitalsignage.agent.application.port.in.FindAgentUseCase;
import io.jeecloud.aidigitalsignage.agent.application.port.out.AgentCachePort;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.agent.domain.AgentRepository;
import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for Agent Query operations (CQRS - Query side).
 * Handles read operations with caching optimization.
 * 
 * Pattern: CQRS Query Side
 * Responsibilities:
 * - Read operations only (no writes)
 * - Cache-first strategy for performance
 * - Delegates to AgentCachePort for caching concerns
 */
@Service
@Transactional(readOnly = true)
public class AgentQueryService implements FindAgentUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentQueryService.class);
    private static final int CACHE_TTL_SECONDS = 3600; // 1 hour
    
    private final AgentRepository agentRepository;
    private final AgentCachePort cachePort;

    public AgentQueryService(AgentRepository agentRepository, AgentCachePort cachePort) {
        this.agentRepository = agentRepository;
        this.cachePort = cachePort;
    }

    @Override
    public Optional<Agent> findByAgentCode(String agentCode) {
        logger.debug("Finding agent by code: {}", agentCode);
        
        AgentCode code = AgentCode.of(agentCode);
        
        // Try cache first
        Optional<Agent> cachedAgent = cachePort.getCachedAgentByCode(code);
        
        if (cachedAgent.isPresent()) {
            logger.debug("Agent found in cache by code: {}", agentCode);
            return cachedAgent;
        }
        
        // Cache miss - query repository
        logger.debug("Cache miss, querying database for agent code: {}", agentCode);
        Optional<Agent> agent = agentRepository.findByAgentCode(code);
        
        // Update cache if found
        agent.ifPresent(a -> cachePort.cacheAgentByCode(code, a, CACHE_TTL_SECONDS));
        
        return agent;
    }

    @Override
    public List<Agent> findByBranchCode(String branchCode) {
        logger.debug("Finding agents by branch code: {}", branchCode);
        return agentRepository.findByBranchCode(branchCode);
    }

    @Override
    public List<Agent> findByChannel(Channel channel) {
        logger.debug("Finding agents by channel: {}", channel);
        return agentRepository.findByChannel(channel);
    }

    @Override
    public List<Agent> findAllActive() {
        logger.debug("Finding all active agents");
        return agentRepository.findAllActive();
    }

    @Override
    public List<Agent> findAllInactive() {
        logger.debug("Finding all inactive agents");
        return agentRepository.findAllInactive();
    }

    @Override
    public List<Agent> findByBranchCodeAndStatus(String branchCode, boolean status) {
        logger.debug("Finding agents by branch code: {} and status: {}", branchCode, status);
        return agentRepository.findByBranchCodeAndStatus(branchCode, status);
    }

    @Override
    public List<Agent> findByChannelAndStatus(Channel channel, boolean status) {
        logger.debug("Finding agents by channel: {} and status: {}", channel, status);
        return agentRepository.findByChannelAndStatus(channel, status);
    }

    @Override
    public List<Agent> findAll() {
        logger.debug("Finding all agents");
        return agentRepository.findAll();
    }

    @Override
    public List<Agent> findByFilters(String branchCode, Channel channel, Boolean active) {
        logger.debug("Finding agents by filters: branchCode={}, channel={}, active={}", 
            branchCode, channel, active);
        return agentRepository.findByFilters(branchCode, channel, active);
    }
}

