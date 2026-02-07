package io.jeecloud.aidigitalsignage.user.domain;

import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import io.jeecloud.aidigitalsignage.common.domain.base.AggregateRoot;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import io.jeecloud.aidigitalsignage.user.domain.event.UserCreatedEvent;
import io.jeecloud.aidigitalsignage.user.domain.event.UserUpdatedEvent;

import java.time.Instant;
import java.util.*;

/**
 * User Aggregate Root.
 * Represents a system user with many-to-many relationship with agents.
 * Uses DDD patterns and maintains consistency boundaries.
 */
public class User implements AggregateRoot<UserId> {
    
    private final UserId userId;
    private final NewNric newNric;
    private String email;
    private String name;
    private boolean status;
    private final Set<AgentCode> associatedAgents;
    private final List<DomainEvent> domainEvents;
    private final Instant createDt;
    private Instant updateDt;

    // Private constructor - use factory methods
    private User(UserId userId, NewNric newNric) {
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null");
        this.newNric = Objects.requireNonNull(newNric, "NewNric cannot be null");
        this.associatedAgents = new HashSet<>();
        this.domainEvents = new ArrayList<>();
        this.createDt = Instant.now();
        this.updateDt = Instant.now();
        this.status = true;
    }

    /**
     * Factory method to create a new User.
     */
    public static User create(
            NewNric newNric,
            String email,
            String name
    ) {
        UserId userId = UserId.of("user-" + UUID.randomUUID().toString());
        User user = new User(userId, newNric);
        user.email = email;
        user.name = name;
        
        // Publish domain event
        user.registerEvent(new UserCreatedEvent(
            userId.getValue(),
            newNric.getValue(),
            email,
            name,
            Instant.now()
        ));
        
        return user;
    }

    /**
     * Factory method to reconstitute a User from persistence.
     */
    public static User reconstitute(
            UserId userId,
            NewNric newNric,
            String email,
            String name,
            boolean status,
            Set<AgentCode> associatedAgents,
            Instant createDt,
            Instant updateDt
    ) {
        User user = new User(userId, newNric);
        user.email = email;
        user.name = name;
        user.status = status;
        if (associatedAgents != null) {
            user.associatedAgents.addAll(associatedAgents);
        }
        // Use reflection or set via field to avoid updating timestamps
        try {
            java.lang.reflect.Field createDtField = User.class.getDeclaredField("createDt");
            createDtField.setAccessible(true);
            createDtField.set(user, createDt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set createDt", e);
        }
        user.updateDt = updateDt;
        
        return user;
    }

    /**
     * Update user details.
     */
    public void update(String email, String name) {
        if (email != null && !email.trim().isEmpty()) {
            this.email = email;
        }
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        this.updateDt = Instant.now();
        
        // Publish domain event
        registerEvent(new UserUpdatedEvent(
            userId.getValue(),
            newNric.getValue(),
            email,
            name,
            Instant.now()
        ));
    }

    /**
     * Update email.
     */
    public void updateEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            this.email = email;
            this.updateDt = Instant.now();
        }
    }

    /**
     * Update name.
     */
    public void updateName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
            this.updateDt = Instant.now();
        }
    }

    /**
     * Associate an agent with this user.
     */
    public void addAgent(AgentCode agentCode) {
        Objects.requireNonNull(agentCode, "AgentCode cannot be null");
        this.associatedAgents.add(agentCode);
        this.updateDt = Instant.now();
    }

    /**
     * Remove agent association.
     */
    public void removeAgent(AgentCode agentCode) {
        Objects.requireNonNull(agentCode, "AgentCode cannot be null");
        this.associatedAgents.remove(agentCode);
        this.updateDt = Instant.now();
    }

    /**
     * Activate the user.
     */
    public void activate() {
        this.status = true;
        this.updateDt = Instant.now();
    }

    /**
     * Deactivate the user.
     */
    public void deactivate() {
        this.status = false;
        this.updateDt = Instant.now();
    }

    // Domain Events
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    // Getters
    @Override
    public UserId getId() {
        return userId;
    }

    public NewNric getNewNric() {
        return newNric;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public boolean isStatus() {
        return status;
    }

    public Set<AgentCode> getAssociatedAgents() {
        return Collections.unmodifiableSet(associatedAgents);
    }

    public Instant getCreateDt() {
        return createDt;
    }

    public Instant getUpdateDt() {
        return updateDt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", newNric=" + newNric +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", agentCount=" + associatedAgents.size() +
                '}';
    }
}
