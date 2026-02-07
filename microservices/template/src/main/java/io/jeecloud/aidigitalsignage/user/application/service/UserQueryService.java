package io.jeecloud.aidigitalsignage.user.application.service;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.user.application.port.in.FindUserUseCase;
import io.jeecloud.aidigitalsignage.user.application.port.out.UserCachePort;
import io.jeecloud.aidigitalsignage.user.domain.NewNric;
import io.jeecloud.aidigitalsignage.user.domain.User;
import io.jeecloud.aidigitalsignage.user.domain.UserId;
import io.jeecloud.aidigitalsignage.user.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for User Query operations (CQRS - Query side).
 * Provides read-only access to user data with caching support.
 */
@Service
@Transactional(readOnly = true)
public class UserQueryService implements FindUserUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(UserQueryService.class);
    
    private final UserRepository userRepository;
    private final UserCachePort cachePort;

    public UserQueryService(
        UserRepository userRepository,
        UserCachePort cachePort
    ) {
        this.userRepository = userRepository;
        this.cachePort = cachePort;
    }

    @Override
    public Optional<User> findById(FindByIdQuery query) {
        logger.debug("Finding user by ID: {}", query.userId());
        
        UserId userId = UserId.of(query.userId());
        
        // Try cache first
        Optional<User> cachedUser = cachePort.findById(userId);
        if (cachedUser.isPresent()) {
            logger.debug("User found in cache: {}", userId);
            return cachedUser;
        }
        
        // Cache miss - query repository
        Optional<User> user = userRepository.findById(userId);
        
        // Update cache if found
        user.ifPresent(cachePort::save);
        
        return user;
    }

    @Override
    public Optional<User> findByNric(FindByNricQuery query) {
        logger.debug("Finding user by NRIC: {}", query.nric());
        
        NewNric newNric = NewNric.of(query.nric());
        Optional<User> user = userRepository.findByNewNric(newNric);
        
        // Update cache if found
        user.ifPresent(cachePort::save);
        
        return user;
    }

    @Override
    public List<User> findByAgentCode(FindByAgentCodeQuery query) {
        logger.debug("Finding users by agent code: {}", query.agentCode());
        
        AgentCode agentCode = AgentCode.of(query.agentCode());
        return userRepository.findByAgentCode(agentCode);
    }

    @Override
    public List<User> findAllActive(FindAllActiveQuery query) {
        logger.debug("Finding all active users");
        
        return userRepository.findAllActive();
    }
}
