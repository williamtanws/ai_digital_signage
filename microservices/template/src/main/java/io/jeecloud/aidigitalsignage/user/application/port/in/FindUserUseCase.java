package io.jeecloud.aidigitalsignage.user.application.port.in;

import io.jeecloud.aidigitalsignage.common.application.common.Query;
import io.jeecloud.aidigitalsignage.user.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Input Port (Use Case) for finding/querying Users.
 * Part of the Application layer in Hexagonal Architecture (CQRS - Query side).
 */
public interface FindUserUseCase {
    
    /**
     * Find a user by ID.
     */
    Optional<User> findById(FindByIdQuery query);
    
    /**
     * Find a user by NRIC.
     */
    Optional<User> findByNric(FindByNricQuery query);
    
    /**
     * Find users by agent code.
     */
    List<User> findByAgentCode(FindByAgentCodeQuery query);
    
    /**
     * Find all active users.
     */
    List<User> findAllActive(FindAllActiveQuery query);
    
    /**
     * Query object for finding by user ID.
     */
    record FindByIdQuery(String userId) implements Query<Optional<User>> {
        public FindByIdQuery {
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("User ID is required");
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public Class<Optional<User>> getResultType() {
            return (Class<Optional<User>>) (Class<?>) Optional.class;
        }
    }
    
    /**
     * Query object for finding by NRIC.
     */
    record FindByNricQuery(String nric) implements Query<Optional<User>> {
        public FindByNricQuery {
            if (nric == null || nric.isBlank()) {
                throw new IllegalArgumentException("NRIC is required");
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public Class<Optional<User>> getResultType() {
            return (Class<Optional<User>>) (Class<?>) Optional.class;
        }
    }
    
    /**
     * Query object for finding by agent code.
     */
    record FindByAgentCodeQuery(String agentCode) implements Query<List<User>> {
        public FindByAgentCodeQuery {
            if (agentCode == null || agentCode.isBlank()) {
                throw new IllegalArgumentException("Agent code is required");
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public Class<List<User>> getResultType() {
            return (Class<List<User>>) (Class<?>) List.class;
        }
    }
    
    /**
     * Query object for finding all active users.
     */
    record FindAllActiveQuery() implements Query<List<User>> {
        @Override
        @SuppressWarnings("unchecked")
        public Class<List<User>> getResultType() {
            return (Class<List<User>>) (Class<?>) List.class;
        }
    }
}
