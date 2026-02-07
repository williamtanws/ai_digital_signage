package io.jeecloud.aidigitalsignage.user.application.service;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;
import io.jeecloud.aidigitalsignage.user.application.port.in.CreateUserUseCase;
import io.jeecloud.aidigitalsignage.user.application.port.in.DeleteUserUseCase;
import io.jeecloud.aidigitalsignage.user.application.port.in.ManageUserAgentUseCase;
import io.jeecloud.aidigitalsignage.user.application.port.in.UpdateUserUseCase;
import io.jeecloud.aidigitalsignage.user.application.port.out.UserCachePort;
import io.jeecloud.aidigitalsignage.user.application.port.out.UserEventPublisher;
import io.jeecloud.aidigitalsignage.user.domain.NewNric;
import io.jeecloud.aidigitalsignage.user.domain.User;
import io.jeecloud.aidigitalsignage.user.domain.UserId;
import io.jeecloud.aidigitalsignage.user.domain.UserRepository;
import io.jeecloud.aidigitalsignage.user.domain.event.UserCreatedEvent;
import io.jeecloud.aidigitalsignage.user.domain.event.UserDeletedEvent;
import io.jeecloud.aidigitalsignage.user.domain.event.UserUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application Service for User Command operations (CQRS - Command side).
 * Orchestrates use cases and coordinates between domain and infrastructure.
 */
@Service
@Transactional
public class UserCommandService implements 
    CreateUserUseCase, 
    UpdateUserUseCase, 
    DeleteUserUseCase,
    ManageUserAgentUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(UserCommandService.class);
    
    private final UserRepository userRepository;
    private final UserEventPublisher eventPublisher;
    private final UserCachePort cachePort;

    public UserCommandService(
        UserRepository userRepository,
        UserEventPublisher eventPublisher,
        UserCachePort cachePort
    ) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.cachePort = cachePort;
    }

    @Override
    public User createUser(CreateUserCommand command) {
        logger.info("Creating user with NRIC: {}", command.newNric());
        
        // Business rule: Email must be unique
        if (userRepository.findByEmail(command.email()).isPresent()) {
            throw new DomainException("User with email " + command.email() + " already exists");
        }
        
        // Business rule: NRIC must be unique
        NewNric newNric = NewNric.of(command.newNric());
        if (userRepository.findByNewNric(newNric).isPresent()) {
            throw new DomainException("User with NRIC " + command.newNric() + " already exists");
        }
        
        // Create domain entity
        User user = User.create(
            newNric,
            command.email(),
            command.name()
        );
        
        // Persist
        User savedUser = userRepository.save(user);
        
        // Publish domain events
        publishDomainEvents(savedUser);
        
        logger.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public User updateUser(UpdateUserCommand command) {
        logger.info("Updating user with ID: {}", command.userId());
        
        // Load user
        UserId userId = UserId.of(command.userId());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new DomainException("User not found: " + command.userId()));
        
        // Update fields
        if (command.email() != null && !command.email().isBlank()) {
            user.updateEmail(command.email());
        }
        if (command.name() != null && !command.name().isBlank()) {
            user.updateName(command.name());
        }
        
        // Persist
        User updatedUser = userRepository.save(user);
        
        // Invalidate cache
        cachePort.invalidate(userId);
        
        // Publish domain events
        publishDomainEvents(updatedUser);
        
        logger.info("User updated successfully: {}", userId);
        return updatedUser;
    }

    @Override
    public void deleteUser(DeleteUserCommand command) {
        logger.info("Deleting user with ID: {}", command.userId());
        
        // Load user
        UserId userId = UserId.of(command.userId());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new DomainException("User not found: " + command.userId()));
        
        // Soft delete
        user.deactivate();
        
        // Persist
        userRepository.save(user);
        
        // Invalidate cache
        cachePort.invalidate(userId);
        
        // Publish domain events
        publishDomainEvents(user);
        
        logger.info("User deleted successfully: {}", userId);
    }

    @Override
    public User addAgentToUser(AddAgentCommand command) {
        logger.info("Adding agent {} to user {}", command.agentCode(), command.userId());
        
        // Load user
        UserId userId = UserId.of(command.userId());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new DomainException("User not found: " + command.userId()));
        
        // Add agent association
        AgentCode agentCode = AgentCode.of(command.agentCode());
        user.addAgent(agentCode);
        
        // Persist
        User updatedUser = userRepository.save(user);
        
        // Invalidate cache
        cachePort.invalidate(userId);
        
        // Publish domain events
        publishDomainEvents(updatedUser);
        
        logger.info("Agent added successfully to user: {}", userId);
        return updatedUser;
    }

    @Override
    public User removeAgentFromUser(RemoveAgentCommand command) {
        logger.info("Removing agent {} from user {}", command.agentCode(), command.userId());
        
        // Load user
        UserId userId = UserId.of(command.userId());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new DomainException("User not found: " + command.userId()));
        
        // Remove agent association
        AgentCode agentCode = AgentCode.of(command.agentCode());
        user.removeAgent(agentCode);
        
        // Persist
        User updatedUser = userRepository.save(user);
        
        // Invalidate cache
        cachePort.invalidate(userId);
        
        // Publish domain events
        publishDomainEvents(updatedUser);
        
        logger.info("Agent removed successfully from user: {}", userId);
        return updatedUser;
    }

    @Override
    public User changeActiveStatus(UpdateUserUseCase.ChangeActiveStatusCommand command) {
        logger.info("Changing active status for user {} to {}", command.userId(), command.status());
        
        // Load user
        UserId userId = UserId.of(command.userId());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new DomainException("User not found: " + command.userId()));
        
        // Change active status
        if (command.status()) {
            user.activate();
        } else {
            user.deactivate();
        }
        
        // Persist
        User updatedUser = userRepository.save(user);
        
        // Invalidate cache
        cachePort.invalidate(userId);
        
        // Publish domain events
        publishDomainEvents(updatedUser);
        
        logger.info("User active status changed successfully: {}", userId);
        return updatedUser;
    }

    /**
     * Publish all domain events collected by the aggregate.
     */
    private void publishDomainEvents(User user) {
        for (DomainEvent event : user.getDomainEvents()) {
            if (event instanceof UserCreatedEvent createdEvent) {
                eventPublisher.publishUserCreated(createdEvent);
            } else if (event instanceof UserUpdatedEvent updatedEvent) {
                eventPublisher.publishUserUpdated(updatedEvent);
            } else if (event instanceof UserDeletedEvent deletedEvent) {
                eventPublisher.publishUserDeleted(deletedEvent);
            }
        }
        user.clearDomainEvents();
    }
}
