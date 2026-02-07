package io.jeecloud.aidigitalsignage.user.domain;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * UserRepository - Repository interface (Port) in Hexagonal Architecture.
 * This is part of the Domain layer and defines the contract for persistence.
 * Implementations will be in the Infrastructure layer.
 */
public interface UserRepository {
    
    /**
     * Save a new user or update an existing one.
     */
    User save(User user);
    
    /**
     * Find a user by its ID.
     */
    Optional<User> findById(UserId userId);
    
    /**
     * Find a user by NRIC.
     */
    Optional<User> findByNewNric(NewNric newNric);
    
    /**
     * Find a user by email.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find all users associated with a specific agent.
     */
    List<User> findByAgentCode(AgentCode agentCode);
    
    /**
     * Find all active users.
     */
    List<User> findAllActive();
    
    /**
     * Find all users.
     */
    List<User> findAll();
    
    /**
     * Delete a user by ID.
     */
    void deleteById(UserId userId);
    
    /**
     * Check if a user exists by NRIC.
     */
    boolean existsByNewNric(NewNric newNric);
    
    /**
     * Check if a user exists by username.
     */
    boolean existsByUsername(String username);
    
    /**
     * Save user-agent associations.
     */
    void saveUserAgentAssociations(UserId userId, Set<AgentCode> agentCodes);
    
    /**
     * Load user-agent associations for a user.
     */
    Set<AgentCode> loadUserAgentAssociations(UserId userId);
}
