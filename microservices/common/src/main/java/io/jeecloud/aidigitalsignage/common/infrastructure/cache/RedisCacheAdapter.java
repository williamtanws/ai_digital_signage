package io.jeecloud.aidigitalsignage.common.infrastructure.cache;

import io.jeecloud.aidigitalsignage.common.application.port.out.CachePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis implementation of the CachePort.
 * 
 * This adapter provides caching capabilities using Redis (AWS ElastiCache in production).
 * It uses JSON serialization for storing complex domain objects.
 * 
 * Pattern: Adapter (Hexagonal Architecture)
 * Location: Shared Infrastructure Layer
 * Technology: Redis / AWS ElastiCache
 * 
 * Key Features:
 * - TTL (Time-To-Live) support for automatic expiration
 * - JSON serialization for complex objects
 * - Null-safe operations
 * - Comprehensive logging for monitoring
 * 
 * Deployment Configuration:
 * - Development: Local Redis (Docker)
 * - UAT: AWS ElastiCache Redis (Single node)
 * - Production: AWS ElastiCache Redis (Cluster mode, Multi-AZ)
 * 
 * Configuration (application.yml):
 * <pre>
 * spring:
 *   data:
 *     redis:
 *       host: ${REDIS_HOST:localhost}
 *       port: ${REDIS_PORT:6379}
 *       password: ${REDIS_PASSWORD:}
 *       ssl:
 *         enabled: ${REDIS_SSL_ENABLED:false}
 *       timeout: 2000ms
 *       lettuce:
 *         pool:
 *           max-active: 10
 *           max-idle: 5
 *           min-idle: 2
 * </pre>
 * 
 * Usage Example:
 * <pre>
 * // Cache an entity for 1 hour
 * cachePort.put("entity:123", entity, 3600);
 * 
 * // Retrieve from cache
 * Optional<MyEntity> cached = cachePort.get("entity:123", MyEntity.class);
 * 
 * // Evict from cache
 * cachePort.evict("entity:123");
 * </pre>
 * 
 * Best Practices:
 * 1. Use meaningful key prefixes (e.g., "entity:id:", "entity:code:")
 * 2. Set appropriate TTL based on data volatility
 * 3. Invalidate cache on write operations (create, update, delete)
 * 4. Handle cache failures gracefully (fallback to database)
 * 5. Monitor cache hit/miss ratio
 * 
 * @see CachePort
 */
@Component
public class RedisCacheAdapter implements CachePort {
    
    private static final Logger log = LoggerFactory.getLogger(RedisCacheAdapter.class);
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public RedisCacheAdapter(RedisTemplate<String, String> redisTemplate, 
                            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void put(String key, Object value) {
        put(key, value, 3600); // Default 1 hour TTL
    }
    
    @Override
    public void put(String key, Object value, int ttlSeconds) {
        if (key == null || value == null) {
            log.warn("Attempted to cache null key or value");
            return;
        }
        
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, Duration.ofSeconds(ttlSeconds));
            log.debug("Cached value with key: {} (TTL: {}s)", key, ttlSeconds);
        } catch (Exception e) {
            log.error("Failed to cache value for key: {}", key, e);
            // Don't throw exception - caching should not break the application
        }
    }
    
    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        if (key == null) {
            log.warn("Attempted to get cache with null key");
            return Optional.empty();
        }
        
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            
            if (jsonValue == null) {
                log.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }
            
            T value = objectMapper.readValue(jsonValue, type);
            log.debug("Cache hit for key: {}", key);
            return Optional.of(value);
            
        } catch (Exception e) {
            log.error("Failed to retrieve cache for key: {}", key, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void evict(String key) {
        if (key == null) {
            log.warn("Attempted to evict cache with null key");
            return;
        }
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Evicted cache for key: {}", key);
            } else {
                log.debug("No cache entry found for key: {}", key);
            }
        } catch (Exception e) {
            log.error("Failed to evict cache for key: {}", key, e);
            // Don't throw exception - eviction failure should not break the application
        }
    }
    
    @Override
    public void clear() {
        try {
            // WARNING: This clears the ENTIRE Redis cache
            // In production, consider using key patterns or separate Redis databases
            log.warn("Clearing entire Redis cache - use with caution!");
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.info("Successfully cleared all cache entries");
        } catch (Exception e) {
            log.error("Failed to clear cache", e);
        }
    }
    
    /**
     * Check if a key exists in cache.
     * Useful for monitoring and diagnostics.
     *
     * @param key The cache key
     * @return true if key exists, false otherwise
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check key existence: {}", key, e);
            return false;
        }
    }
    
    /**
     * Get remaining TTL for a key.
     * Useful for monitoring and diagnostics.
     *
     * @param key The cache key
     * @return Remaining TTL in seconds, or -1 if key doesn't exist
     */
    public long getTTL(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("Failed to get TTL for key: {}", key, e);
            return -1;
        }
    }
}
