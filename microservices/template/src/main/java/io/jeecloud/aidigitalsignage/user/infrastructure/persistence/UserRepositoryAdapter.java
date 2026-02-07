package io.jeecloud.aidigitalsignage.user.infrastructure.persistence;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.user.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Repository adapter for User aggregate.
 * Implements the domain UserRepository interface using a mix of JPA and EntityManager repositories.
 * 
 * <p>This adapter follows Hexagonal Architecture principles by:
 * <ul>
 *   <li>Implementing domain repository interface (port)</li>
 *   <li>Bridging domain layer with infrastructure (JPA persistence)</li>
 *   <li>Handling entity-domain model mapping</li>
 *   <li>Managing user-agent associations</li>
 * </ul>
 * 
 * <p>Repository Strategy:
 * <ul>
 *   <li>JPA Repository: save, findByNewNric, findByEmail, findAll, delete operations</li>
 *   <li>EntityManager Repository: findById, findByUsername, findAllActive, exists checks</li>
 * </ul>
 * 
 * @see UserRepository
 * @see UserEntity
 * @see UserEntityManagerRepository
 * @see UserJpaRepository
 */
@Component
public class UserRepositoryAdapter implements UserRepository {
    
    // Mix of EntityManager and JPA repositories
    private final UserEntityManagerRepository userEntityManagerRepository;
    private final UserJpaRepository userJpaRepository;
    private final UserAgentEntityManagerRepository userAgentEntityManagerRepository;
    private final UserAgentJpaRepository userAgentJpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserRepositoryAdapter(
            UserEntityManagerRepository userEntityManagerRepository,
            UserJpaRepository userJpaRepository,
            UserAgentEntityManagerRepository userAgentEntityManagerRepository,
            UserAgentJpaRepository userAgentJpaRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userEntityManagerRepository = userEntityManagerRepository;
        this.userJpaRepository = userJpaRepository;
        this.userAgentEntityManagerRepository = userAgentEntityManagerRepository;
        this.userAgentJpaRepository = userAgentJpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity = toEntity(user);
        // Use JPA Repository for save operations
        UserEntity savedEntity = userJpaRepository.save(entity);
        
        // Only save user-agent associations if they were explicitly loaded/modified
        // Empty set could mean either: (1) no agents, or (2) agents weren't loaded
        // To distinguish, we check if the user has associations loaded by comparing with existing
        Set<AgentCode> currentAgents = user.getAssociatedAgents();
        Set<AgentCode> existingAgents = loadUserAgentAssociations(user.getId());
        
        // Only update associations if they differ from what's in the database
        // This prevents accidental deletion when agents weren't loaded
        if (!currentAgents.equals(existingAgents)) {
            saveUserAgentAssociations(user.getId(), currentAgents);
        }
        
        // Publish domain events for TransactionalEventListener
        user.getDomainEvents().forEach(eventPublisher::publishEvent);
        
        // Also publish the User entity itself for status change events
        // This will be picked up by UserStatusChangedListener
        eventPublisher.publishEvent(user);
        
        // Return with loaded associations
        return toDomain(savedEntity, loadUserAgentAssociations(user.getId()));
    }

    @Override
    public Optional<User> findById(UserId userId) {
        // Use EntityManager Repository for findById
        return userEntityManagerRepository.findById(userId.getValue())
            .map(this::enrichEntityWithAssociations);
    }

    @Override
    public Optional<User> findByNewNric(NewNric newNric) {
        // Use JPA Repository for findByNewNric
        return userJpaRepository.findByNewNric(newNric.getValue())
            .map(this::enrichEntityWithAssociations);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // Use JPA Repository for findByEmail
        return userJpaRepository.findByEmail(email)
            .map(this::enrichEntityWithAssociations);
    }

    @Override
    public List<User> findByAgentCode(AgentCode agentCode) {
        // Use JPA Repository for associations
        List<UserAgentEntity> associations = userAgentJpaRepository.findByAgentCode(agentCode.value());
        
        return associations.stream()
            .map(ua -> findById(UserId.of(ua.getId().getUserId())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    @Override
    public List<User> findAllActive() {
        // Use EntityManager Repository for findAllActive
        return userEntityManagerRepository.findAllActive().stream()
            .map(this::enrichEntityWithAssociations)
            .collect(Collectors.toList());
    }

    @Override
    public List<User> findAll() {
        // Use JPA Repository for findAll
        return userJpaRepository.findAll().stream()
            .map(this::enrichEntityWithAssociations)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UserId userId) {
        // Delete associations first - use EntityManager Repository
        userAgentEntityManagerRepository.deleteByUserId(userId.getValue());
        // Then delete user - use JPA Repository
        userJpaRepository.deleteById(userId.getValue());
    }

    @Override
    public boolean existsByNewNric(NewNric newNric) {
        // Use JPA Repository for existsByNewNric
        return userJpaRepository.existsByNewNric(newNric.getValue());
    }

    @Override
    public boolean existsByUsername(String username) {
        // Use EntityManager Repository for existsByUsername
        return userEntityManagerRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public void saveUserAgentAssociations(UserId userId, Set<AgentCode> agentCodes) {
        // Delete existing associations - use JPA Repository
        userAgentJpaRepository.deleteByUserId(userId.getValue());
        
        // Create new associations - use EntityManager Repository
        List<UserAgentEntity> associations = agentCodes.stream()
            .map(agentCode -> UserAgentEntity.builder()
                .id(new UserAgentEntity.UserAgentId(userId.getValue(), agentCode.value()))
                .build())
            .collect(Collectors.toList());
        
        userAgentEntityManagerRepository.saveAll(associations);
    }

    @Override
    public Set<AgentCode> loadUserAgentAssociations(UserId userId) {
        // Use EntityManager Repository for loading associations
        List<UserAgentEntity> associations = userAgentEntityManagerRepository.findByUserId(userId.getValue());
        
        return associations.stream()
            .map(ua -> AgentCode.of(ua.getId().getAgentCode()))
            .collect(Collectors.toSet());
    }

    // Helper methods
    
    /**
     * Enriches a user entity with its agent associations.
     * Helper method to reduce code duplication.
     *
     * @param entity the user entity to enrich
     * @return the domain user with loaded associations
     */
    private User enrichEntityWithAssociations(UserEntity entity) {
        UserId userId = UserId.of(entity.getUserId());
        Set<AgentCode> agentCodes = loadUserAgentAssociations(userId);
        return toDomain(entity, agentCodes);
    }
    
    // Mapping methods
    private UserEntity toEntity(User user) {
        return UserEntity.builder()
            .userId(user.getId().getValue())
            .newNric(user.getNewNric().getValue())
            .email(user.getEmail())
            .name(user.getName())
            .status(user.isStatus())
            .createDt(user.getCreateDt())
            .updateDt(user.getUpdateDt())
            .build();
    }

    /**
     * Maps a user entity to domain model.
     * Simple mapping without associations.
     *
     * @param entity the user entity
     * @return the domain user without associations
     */
    private User toDomain(UserEntity entity) {
        return toDomain(entity, new HashSet<>());
    }

    /**
     * Maps a user entity to domain model with agent associations.
     *
     * @param entity the user entity
     * @param agentCodes the associated agent codes
     * @return the domain user with associations
     */
    private User toDomain(UserEntity entity, Set<AgentCode> agentCodes) {
        return User.reconstitute(
            UserId.of(entity.getUserId()),
            NewNric.of(entity.getNewNric()),
            entity.getEmail(),
            entity.getName(),
            entity.isStatus(),
            agentCodes,
            entity.getCreateDt(),
            entity.getUpdateDt()
        );
    }
}
