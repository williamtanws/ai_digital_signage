package io.jeecloud.aidigitalsignage.user.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * EntityManager-based repository for UserAgentEntity.
 * Manages the many-to-many relationship between Users and Agents using pure JPA.
 */
@Repository
public class UserAgentEntityManagerRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public UserAgentEntity save(UserAgentEntity entity) {
        return entityManager.merge(entity);
    }

    @Transactional
    public List<UserAgentEntity> saveAll(List<UserAgentEntity> entities) {
        return entities.stream()
            .map(entityManager::merge)
            .toList();
    }

    public List<UserAgentEntity> findByUserId(String userId) {
        TypedQuery<UserAgentEntity> query = entityManager.createQuery(
            "SELECT ua FROM UserAgentEntity ua WHERE ua.id.userId = :userId", 
            UserAgentEntity.class
        );
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<String> findAgentCodesByUserId(String userId) {
        TypedQuery<String> query = entityManager.createQuery(
            "SELECT ua.id.agentCode FROM UserAgentEntity ua WHERE ua.id.userId = :userId", 
            String.class
        );
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<UserAgentEntity> findByAgentCode(String agentCode) {
        TypedQuery<UserAgentEntity> query = entityManager.createQuery(
            "SELECT ua FROM UserAgentEntity ua WHERE ua.id.agentCode = :agentCode", 
            UserAgentEntity.class
        );
        query.setParameter("agentCode", agentCode);
        return query.getResultList();
    }

    @Transactional
    public void deleteByUserId(String userId) {
        entityManager.createQuery(
            "DELETE FROM UserAgentEntity ua WHERE ua.id.userId = :userId"
        )
        .setParameter("userId", userId)
        .executeUpdate();
    }
}
