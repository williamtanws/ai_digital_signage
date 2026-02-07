package io.jeecloud.aidigitalsignage.user.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * EntityManager-based repository for UserEntity.
 * Uses pure JPA without Spring Data JPA abstractions.
 * 
 * Implements various EntityManager patterns:
 * - JPQL queries
 * - Criteria API
 * - Named queries
 * - Native SQL
 * - Transaction management
 * - Batch operations
 */
@Repository
public class UserEntityManagerRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    // ==================== CRUD Operations ====================
    
    /**
     * Save or update a user entity.
     * Demonstrates persist vs merge operations.
     */
    @Transactional
    public UserEntity save(UserEntity entity) {
        if (entity.getUserId() == null) {
            // New entity - use persist
            entityManager.persist(entity);
            return entity;
        } else {
            // Existing entity - use merge
            return entityManager.merge(entity);
        }
    }

    /**
     * Find user by ID using EntityManager.find()
     * This is the simplest way to retrieve entities by primary key.
     */
    public Optional<UserEntity> findById(String id) {
        try {
            UserEntity entity = entityManager.find(UserEntity.class, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Delete user by ID.
     * Demonstrates remove operation with find.
     */
    @Transactional
    public void deleteById(String id) {
        UserEntity entity = entityManager.find(UserEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    // ==================== JPQL Queries ====================
    
    /**
     * Find user by NRIC using JPQL (Java Persistence Query Language).
     * JPQL is object-oriented and works with entity classes.
     */
    public Optional<UserEntity> findByNewNric(String newNric) {
        try {
            TypedQuery<UserEntity> query = entityManager.createQuery(
                "SELECT u FROM UserEntity u WHERE u.newNric = :newNric", 
                UserEntity.class
            );
            query.setParameter("newNric", newNric);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Find user by username using JPQL.
     */
    public Optional<UserEntity> findByUsername(String username) {
        try {
            String jpql = "SELECT u FROM UserEntity u WHERE u.username = :username";
            TypedQuery<UserEntity> query = entityManager.createQuery(jpql, UserEntity.class);
            query.setParameter("username", username);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Find user by email using JPQL.
     */
    public Optional<UserEntity> findByEmail(String email) {
        try {
            String jpql = "SELECT u FROM UserEntity u WHERE u.email = :email";
            TypedQuery<UserEntity> query = entityManager.createQuery(jpql, UserEntity.class);
            query.setParameter("email", email);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Check if user exists by NRIC.
     */
    public boolean existsByNewNric(String newNric) {
        String jpql = "SELECT COUNT(u) FROM UserEntity u WHERE u.newNric = :newNric";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("newNric", newNric);
        return query.getSingleResult() > 0;
    }

    /**
     * Check if user exists by username.
     */
    public boolean existsByUsername(String username) {
        String jpql = "SELECT COUNT(u) FROM UserEntity u WHERE u.username = :username";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("username", username);
        return query.getSingleResult() > 0;
    }

    /**
     * Find all active users using JPQL.
     * Demonstrates list results.
     */
    public List<UserEntity> findAllActive() {
        String jpql = "SELECT u FROM UserEntity u WHERE u.status = :status ORDER BY u.email";
        TypedQuery<UserEntity> query = entityManager.createQuery(jpql, UserEntity.class);
        query.setParameter("status", true);
        return query.getResultList();
    }

    /**
     * Find all users.
     */
    public List<UserEntity> findAll() {
        String jpql = "SELECT u FROM UserEntity u ORDER BY u.username";
        return entityManager.createQuery(jpql, UserEntity.class).getResultList();
    }

    /**
     * Find all users with pagination using JPQL.
     * Demonstrates setFirstResult and setMaxResults for pagination.
     */
    public List<UserEntity> findAllWithPagination(int page, int size) {
        String jpql = "SELECT u FROM UserEntity u ORDER BY u.createDt DESC";
        TypedQuery<UserEntity> query = entityManager.createQuery(jpql, UserEntity.class);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    /**
     * Count total users using JPQL aggregate function.
     */
    public long countAll() {
        String jpql = "SELECT COUNT(u) FROM UserEntity u";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        return query.getSingleResult();
    }

    /**
     * Find users by email domain using JPQL LIKE operator.
     */
    public List<UserEntity> findByEmailDomain(String domain) {
        String jpql = "SELECT u FROM UserEntity u WHERE u.email LIKE :pattern";
        TypedQuery<UserEntity> query = entityManager.createQuery(jpql, UserEntity.class);
        query.setParameter("pattern", "%@" + domain);
        return query.getResultList();
    }

    // ==================== Criteria API ====================
    
    /**
     * Find users by multiple criteria using Criteria API.
     * The Criteria API provides type-safe, programmatic query construction.
     * This is more flexible than JPQL for dynamic queries.
     */
    public List<UserEntity> findByCriteria(String username, String email, Boolean active) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> cq = cb.createQuery(UserEntity.class);
        Root<UserEntity> user = cq.from(UserEntity.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (username != null && !username.isEmpty()) {
            predicates.add(cb.like(cb.lower(user.get("username")), "%" + username.toLowerCase() + "%"));
        }
        
        if (email != null && !email.isEmpty()) {
            predicates.add(cb.like(cb.lower(user.get("email")), "%" + email.toLowerCase() + "%"));
        }
        
        if (active != null) {
            predicates.add(cb.equal(user.get("active"), active));
        }
        
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(user.get("username")));
        
        TypedQuery<UserEntity> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    /**
     * Count users by criteria using Criteria API.
     */
    public long countByCriteria(Boolean active) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<UserEntity> user = cq.from(UserEntity.class);
        
        cq.select(cb.count(user));
        
        if (active != null) {
            cq.where(cb.equal(user.get("active"), active));
        }
        
        TypedQuery<Long> query = entityManager.createQuery(cq);
        return query.getSingleResult();
    }

    // ==================== Native SQL ====================
    
    /**
     * Execute native SQL query.
     * Use this when JPQL is not sufficient or for database-specific features.
     */
    @SuppressWarnings("unchecked")
    public List<UserEntity> findByNativeQuery(String searchTerm) {
        String sql = "SELECT * FROM users u WHERE " +
                     "LOWER(u.username) LIKE LOWER(:search) OR " +
                     "LOWER(u.full_name) LIKE LOWER(:search) " +
                     "ORDER BY u.username";
        
        return entityManager.createNativeQuery(sql, UserEntity.class)
                .setParameter("search", "%" + searchTerm + "%")
                .getResultList();
    }

    /**
     * Execute native SQL for aggregate results.
     * Returns scalar values instead of entities.
     */
    public long countActiveUsersNative() {
        String sql = "SELECT COUNT(*) FROM users WHERE active = true";
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }

    // ==================== Batch Operations ====================
    
    /**
     * Batch update using JPQL.
     * Demonstrates bulk update operation.
     */
    @Transactional
    public int deactivateUsersByEmailDomain(String domain) {
        String jpql = "UPDATE UserEntity u SET u.status = false " +
                     "WHERE u.email LIKE :pattern";
        return entityManager.createQuery(jpql)
                .setParameter("pattern", "%@" + domain)
                .executeUpdate();
    }

    /**
     * Batch delete using JPQL.
     */
    @Transactional
    public int deleteInactiveUsers() {
        String jpql = "DELETE FROM UserEntity u WHERE u.status = false";
        return entityManager.createQuery(jpql).executeUpdate();
    }

    /**
     * Batch insert demonstration.
     * Shows proper batch processing with flush and clear.
     */
    @Transactional
    public void batchInsert(List<UserEntity> users) {
        int batchSize = 20;
        for (int i = 0; i < users.size(); i++) {
            entityManager.persist(users.get(i));
            
            if (i % batchSize == 0 && i > 0) {
                // Flush a batch of inserts and release memory
                entityManager.flush();
                entityManager.clear();
            }
        }
        // Flush remaining entities
        entityManager.flush();
        entityManager.clear();
    }

    // ==================== Advanced Operations ====================
    
    /**
     * Refresh entity from database.
     * Useful to discard in-memory changes and reload from database.
     */
    @Transactional
    public void refresh(UserEntity entity) {
        entityManager.refresh(entity);
    }

    /**
     * Detach entity from persistence context.
     * Useful when you want to modify entity without persisting changes.
     */
    public void detach(UserEntity entity) {
        entityManager.detach(entity);
    }

    /**
     * Check if entity is managed in current persistence context.
     */
    public boolean contains(UserEntity entity) {
        return entityManager.contains(entity);
    }

    /**
     * Flush pending changes to database.
     * Usually called automatically at transaction commit,
     * but can be called manually for immediate execution.
     */
    @Transactional
    public void flush() {
        entityManager.flush();
    }

    /**
     * Clear persistence context.
     * Detaches all managed entities.
     */
    public void clear() {
        entityManager.clear();
    }

    // ==================== Named Queries Example ====================
    
    /**
     * Example of using named queries (would need to be defined on the entity).
     * Named queries are defined using @NamedQuery annotation on the entity class.
     * They are pre-compiled and validated at startup.
     */
    public List<UserEntity> findUsingNamedQuery(boolean active) {
        // This would work if the entity has @NamedQuery defined
        // @NamedQuery(name = "UserEntity.findByActive", query = "SELECT u FROM UserEntity u WHERE u.status = :status")
        try {
            return entityManager.createNamedQuery("UserEntity.findByActive", UserEntity.class)
                    .setParameter("active", active)
                    .getResultList();
        } catch (Exception e) {
            // Named query not defined, return empty list
            return new ArrayList<>();
        }
    }

    // ==================== Transaction Management Examples ====================
    
    /**
     * Example of nested transaction handling.
     * Demonstrates how to work within existing transaction.
     */
    @Transactional
    public UserEntity saveWithLogging(UserEntity entity) {
        // This method participates in existing transaction or creates new one
        UserEntity saved = save(entity);
        
        // Log the operation (would be part of same transaction)
        System.out.println("User saved: " + saved.getEmail());
        
        return saved;
    }

    /**
     * Example of read-only operation.
     * Read-only transactions can be optimized by the JPA provider.
     */
    @Transactional(readOnly = true)
    public Optional<UserEntity> findByIdReadOnly(String id) {
        return findById(id);
    }
}
