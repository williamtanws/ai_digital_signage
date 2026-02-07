package io.jeecloud.aidigitalsignage.agent.infrastructure.cache;

import io.jeecloud.aidigitalsignage.agent.application.port.out.AgentCachePort;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.common.application.port.out.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Cache Adapter for Agent-specific caching operations.
 * 
 * This adapter implements the AgentCachePort using the generic CachePort infrastructure.
 * It provides a component-specific caching layer with business-meaningful keys.
 * 
 * Pattern: Adapter (Hexagonal Architecture)
 * Location: Infrastructure Layer - Secondary Adapter
 * Technology: Delegates to generic CachePort (which uses Redis/Caffeine)
 * 
 * Example Usage in AgentQueryService:
 * <pre>
 * public Optional<Agent> findById(AgentId id) {
 *     // Try cache first (CQRS read optimization)
 *     Optional<Agent> cached = agentCachePort.getCachedAgent(id);
 *     if (cached.isPresent()) {
 *         return cached;
 *     }
 *     
 *     // Fetch from database
 *     Optional<Agent> agent = agentRepository.findById(id);
 *     
 *     // Cache for future queries
 *     agent.ifPresent(a -> agentCachePort.cacheAgent(id, a, 3600));
 *     
 *     return agent;
 * }
 * </pre>
 * 
 * @see AgentCachePort
 * @see io.jeecloud.aidigitalsignage.shared.application.port.out.CachePort
 */
@Component
public class AgentCacheAdapter implements AgentCachePort {
    
    private static final Logger log = LoggerFactory.getLogger(AgentCacheAdapter.class);
    private static final String AGENT_BY_CODE_PREFIX = "agent:code:";
    
    private final CachePort cachePort;
    
    public AgentCacheAdapter(CachePort cachePort) {
        this.cachePort = cachePort;
    }
    
    @Override
    public void cacheAgent(AgentCode agentCode, Agent agent, int ttlSeconds) {
        String key = AGENT_BY_CODE_PREFIX + agentCode.value();
        cachePort.put(key, agent, ttlSeconds);
        log.debug("Cached agent with code: {}", agentCode.value());
    }
    
    @Override
    public Optional<Agent> getCachedAgent(AgentCode agentCode) {
        String key = AGENT_BY_CODE_PREFIX + agentCode.value();
        Optional<Agent> cached = cachePort.get(key, Agent.class);
        if (cached.isPresent()) {
            log.debug("Cache hit for agent code: {}", agentCode.value());
        } else {
            log.debug("Cache miss for agent code: {}", agentCode.value());
        }
        return cached;
    }
    
    @Override
    public void cacheAgentByCode(AgentCode agentCode, Agent agent, int ttlSeconds) {
        String key = AGENT_BY_CODE_PREFIX + agentCode.value();
        cachePort.put(key, agent, ttlSeconds);
        log.debug("Cached agent with code: {}", agentCode.value());
    }
    
    @Override
    public Optional<Agent> getCachedAgentByCode(AgentCode agentCode) {
        String key = AGENT_BY_CODE_PREFIX + agentCode.value();
        Optional<Agent> cached = cachePort.get(key, Agent.class);
        if (cached.isPresent()) {
            log.debug("Cache hit for agent code: {}", agentCode.value());
        } else {
            log.debug("Cache miss for agent code: {}", agentCode.value());
        }
        return cached;
    }
    
    @Override
    public void invalidateAgent(AgentCode agentCode) {
        String key = AGENT_BY_CODE_PREFIX + agentCode.value();
        cachePort.evict(key);
        log.debug("Invalidated cache for agent code: {}", agentCode.value());
    }
    
    @Override
    public void invalidateAgentByCode(AgentCode agentCode) {
        String key = AGENT_BY_CODE_PREFIX + agentCode.value();
        cachePort.evict(key);
        log.debug("Invalidated cache for agent code: {}", agentCode.value());
    }
    
    @Override
    public void clearAllAgentCache() {
        // In a real implementation, you might use Redis SCAN with pattern matching
        // For now, this is a placeholder for the concept
        log.info("Clearing all agent cache entries");
        // cachePort.evictPattern(AGENT_BY_CODE_PREFIX + "*");
    }
}
