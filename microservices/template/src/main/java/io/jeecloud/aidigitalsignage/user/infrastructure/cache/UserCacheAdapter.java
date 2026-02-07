package io.jeecloud.aidigitalsignage.user.infrastructure.cache;

import io.jeecloud.aidigitalsignage.common.application.port.out.CachePort;
import io.jeecloud.aidigitalsignage.user.application.port.out.UserCachePort;
import io.jeecloud.aidigitalsignage.user.domain.User;
import io.jeecloud.aidigitalsignage.user.domain.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Cache Adapter for User-specific caching operations.
 * 
 * This adapter implements the UserCachePort using the generic CachePort infrastructure.
 * It provides a component-specific caching layer with business-meaningful keys.
 * 
 * Pattern: Adapter (Hexagonal Architecture)
 * Location: Infrastructure Layer - Secondary Adapter
 * Technology: Delegates to generic CachePort (which uses Redis/Caffeine)
 * 
 * Example Usage in UserQueryService:
 * <pre>
 * public Optional<User> findById(UserId id) {
 *     // Try cache first (CQRS read optimization)
 *     Optional<User> cached = userCachePort.findById(id);
 *     if (cached.isPresent()) {
 *         return cached;
 *     }
 *     
 *     // Fetch from database
 *     Optional<User> user = userRepository.findById(id);
 *     
 *     // Cache for future queries
 *     user.ifPresent(u -> userCachePort.save(u));
 *     
 *     return user;
 * }
 * </pre>
 * 
 * @see UserCachePort
 * @see io.jeecloud.aidigitalsignage.common.application.port.out.CachePort
 */
@Component
public class UserCacheAdapter implements UserCachePort {
    
    private static final Logger log = LoggerFactory.getLogger(UserCacheAdapter.class);
    private static final String USER_BY_ID_PREFIX = "user:id:";
    private static final int DEFAULT_TTL_SECONDS = 3600; // 1 hour
    
    private final CachePort cachePort;
    
    public UserCacheAdapter(CachePort cachePort) {
        this.cachePort = cachePort;
    }
    
    @Override
    public void save(User user) {
        String key = USER_BY_ID_PREFIX + user.getId().getValue();
        cachePort.put(key, user, DEFAULT_TTL_SECONDS);
        log.debug("Cached user with ID: {}", user.getId().getValue());
    }
    
    @Override
    public Optional<User> findById(UserId userId) {
        String key = USER_BY_ID_PREFIX + userId.getValue();
        Optional<User> cached = cachePort.get(key, User.class);
        if (cached.isPresent()) {
            log.debug("Cache hit for user ID: {}", userId.getValue());
        } else {
            log.debug("Cache miss for user ID: {}", userId.getValue());
        }
        return cached;
    }
    
    @Override
    public void invalidate(UserId userId) {
        String key = USER_BY_ID_PREFIX + userId.getValue();
        cachePort.evict(key);
        log.debug("Invalidated cache for user ID: {}", userId.getValue());
    }
    
    @Override
    public void clearAll() {
        // In a real implementation, you might use Redis SCAN with pattern matching
        // For now, this is a placeholder for the concept
        log.info("Clearing all user cache entries");
        // cachePort.evictPattern(USER_BY_ID_PREFIX + "*");
    }
}
