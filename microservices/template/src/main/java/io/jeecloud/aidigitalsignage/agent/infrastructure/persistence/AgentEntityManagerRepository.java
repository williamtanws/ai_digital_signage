package io.jeecloud.aidigitalsignage.agent.infrastructure.persistence;

import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * EntityManager-based repository for AgentEntity.
 * Uses pure JPA without Spring Data JPA abstractions.
 * 
 * Implements various EntityManager patterns:
 * - JPQL queries with enums
 * - Criteria API with complex conditions
 * - Pessimistic and optimistic locking
 * - Stored procedure calls
 * - Join queries
 * - Subqueries
 * - Query hints and performance optimization
 */
@Repository
public class AgentEntityManagerRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    // ==================== CRUD Operations ====================
    
    /**
     * Save or update an agent entity.
     */
    @Transactional
    public AgentEntity save(AgentEntity entity) {
        if (entity.getAgentCode() == null || !existsByAgentCode(entity.getAgentCode())) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }

    /**
     * Find agent by ID (agent code).
     */
    public Optional<AgentEntity> findById(String agentCode) {
        try {
            AgentEntity entity = entityManager.find(AgentEntity.class, agentCode);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Find agent by agent code using JPQL.
     */
    public Optional<AgentEntity> findByAgentCode(String agentCode) {
        try {
            String jpql = "SELECT a FROM AgentEntity a WHERE a.agentCode = :agentCode";
            TypedQuery<AgentEntity> query = entityManager.createQuery(jpql, AgentEntity.class);
            query.setParameter("agentCode", agentCode);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Find with pessimistic lock.
     * Useful for preventing concurrent modifications.
     */
    @Transactional
    public Optional<AgentEntity> findByIdWithLock(String agentCode) {
        try {
            AgentEntity entity = entityManager.find(
                AgentEntity.class, 
                agentCode, 
                LockModeType.PESSIMISTIC_WRITE
            );
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Delete agent by ID.
     */
    @Transactional
    public void deleteById(String agentCode) {
        AgentEntity entity = entityManager.find(AgentEntity.class, agentCode);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    // ==================== JPQL Queries with Enums ====================
    
    /**
     * Find agents by channel using JPQL.
     * Demonstrates working with enum types in queries.
     */
    public List<AgentEntity> findByChannel(Channel channel) {
        String jpql = "SELECT a FROM AgentEntity a WHERE a.channel = :channel ORDER BY a.name";
        TypedQuery<AgentEntity> query = entityManager.createQuery(jpql, AgentEntity.class);
        query.setParameter("channel", channel);
        return query.getResultList();
    }

    /**
     * Find agents by multiple channels.
     */
    public List<AgentEntity> findByChannels(List<Channel> channels) {
        String jpql = "SELECT a FROM AgentEntity a WHERE a.channel IN :channels ORDER BY a.name";
        TypedQuery<AgentEntity> query = entityManager.createQuery(jpql, AgentEntity.class);
        query.setParameter("channels", channels);
        return query.getResultList();
    }

    /**
     * Find agents by branch code.
     */
    public List<AgentEntity> findByBranchCode(String branchCode) {
        String jpql = "SELECT a FROM AgentEntity a WHERE a.branchCode = :branchCode";
        TypedQuery<AgentEntity> query = entityManager.createQuery(jpql, AgentEntity.class);
        query.setParameter("branchCode", branchCode);
        return query.getResultList();
    }

    /**
     * Find all active agents.
     */
    public List<AgentEntity> findAllActive() {
        String jpql = "SELECT a FROM AgentEntity a WHERE a.status = true ORDER BY a.name";
        return entityManager.createQuery(jpql, AgentEntity.class).getResultList();
    }

    // ==================== Advanced JPQL ====================
    
    /**
     * Find agents with case-insensitive name search.
     */
    public List<AgentEntity> findByNameContaining(String name) {
        String jpql = "SELECT a FROM AgentEntity a WHERE LOWER(a.name) LIKE LOWER(:name)";
        TypedQuery<AgentEntity> query = entityManager.createQuery(jpql, AgentEntity.class);
        query.setParameter("name", "%" + name + "%");
        return query.getResultList();
    }

    /**
     * Find agents created within date range.
     */
    public List<AgentEntity> findCreatedBetween(Instant startDate, Instant endDate) {
        String jpql = "SELECT a FROM AgentEntity a WHERE a.createDt BETWEEN :start AND :end";
        TypedQuery<AgentEntity> query = entityManager.createQuery(jpql, AgentEntity.class);
        query.setParameter("start", startDate);
        query.setParameter("end", endDate);
        return query.getResultList();
    }

    /**
     * Count agents by channel.
     */
    public long countByChannel(Channel channel) {
        String jpql = "SELECT COUNT(a) FROM AgentEntity a WHERE a.channel = :channel";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("channel", channel);
        return query.getSingleResult();
    }

    /**
     * Get distinct branch codes.
     */
    public List<String> findDistinctBranchCodes() {
        String jpql = "SELECT DISTINCT a.branchCode FROM AgentEntity a ORDER BY a.branchCode";
        return entityManager.createQuery(jpql, String.class).getResultList();
    }

    // ==================== Criteria API ====================
    
    /**
     * Dynamic query using Criteria API.
     * Build query based on available parameters.
     */
    public List<AgentEntity> findByCriteria(
            String agentCode, 
            String name, 
            String branchCode, 
            Channel channel, 
            Boolean active) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AgentEntity> cq = cb.createQuery(AgentEntity.class);
        Root<AgentEntity> agent = cq.from(AgentEntity.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (agentCode != null && !agentCode.isEmpty()) {
            predicates.add(cb.like(agent.get("agentCode"), agentCode + "%"));
        }
        
        if (name != null && !name.isEmpty()) {
            predicates.add(cb.like(cb.lower(agent.get("name")), "%" + name.toLowerCase() + "%"));
        }
        
        if (branchCode != null && !branchCode.isEmpty()) {
            predicates.add(cb.equal(agent.get("branchCode"), branchCode));
        }
        
        if (channel != null) {
            predicates.add(cb.equal(agent.get("channel"), channel));
        }
        
        if (active != null) {
            predicates.add(cb.equal(agent.get("active"), active));
        }
        
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(agent.get("name")));
        
        TypedQuery<AgentEntity> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    /**
     * Complex criteria with OR conditions.
     */
    public List<AgentEntity> findByNameOrBranchCode(String searchTerm) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AgentEntity> cq = cb.createQuery(AgentEntity.class);
        Root<AgentEntity> agent = cq.from(AgentEntity.class);
        
        Predicate namePredicate = cb.like(cb.lower(agent.get("name")), "%" + searchTerm.toLowerCase() + "%");
        Predicate branchPredicate = cb.like(agent.get("branchCode"), searchTerm + "%");
        
        cq.where(cb.or(namePredicate, branchPredicate));
        cq.orderBy(cb.asc(agent.get("name")));
        
        return entityManager.createQuery(cq).getResultList();
    }

    // ==================== Native SQL ====================
    
    /**
     * Complex native query with joins.
     * Example showing how to work with native SQL when needed.
     */
    @SuppressWarnings("unchecked")
    public List<AgentEntity> findWithStatisticsNative() {
        String sql = "SELECT a.* FROM agents a " +
                     "WHERE a.status = true " +
                     "ORDER BY a.create_dt DESC " +
                     "LIMIT 100";
        
        return entityManager.createNativeQuery(sql, AgentEntity.class).getResultList();
    }

    /**
     * Native query returning custom projection.
     */
    public List<Object[]> getAgentStatisticsByChannel() {
        String sql = "SELECT channel, COUNT(*) as agent_count, " +
                     "COUNT(CASE WHEN status = true THEN 1 END) as active_count " +
                     "FROM agents " +
                     "GROUP BY channel " +
                     "ORDER BY channel";
        
        return entityManager.createNativeQuery(sql).getResultList();
    }

    // ==================== Batch Operations ====================
    
    /**
     * Batch update active status.
     */
    @Transactional
    public int updateActiveStatusByBranch(String branchCode, boolean status) {
        String jpql = "UPDATE AgentEntity a SET a.status = :status " +
                     "WHERE a.branchCode = :branchCode";
        return entityManager.createQuery(jpql)
                .setParameter("status", status)
                .setParameter("branchCode", branchCode)
                .executeUpdate();
    }

    /**
     * Batch update channel.
     */
    @Transactional
    public int updateChannelByBranch(String branchCode, Channel newChannel) {
        String jpql = "UPDATE AgentEntity a SET a.channel = :channel, a.updateDt = :now " +
                     "WHERE a.branchCode = :branchCode";
        return entityManager.createQuery(jpql)
                .setParameter("channel", newChannel)
                .setParameter("branchCode", branchCode)
                .setParameter("now", Instant.now())
                .executeUpdate();
    }

    /**
     * Batch delete inactive agents.
     */
    @Transactional
    public int deleteInactiveAgents(Instant olderThan) {
        String jpql = "DELETE FROM AgentEntity a " +
                     "WHERE a.status = false AND a.updateDt < :date";
        return entityManager.createQuery(jpql)
                .setParameter("date", olderThan)
                .executeUpdate();
    }

    // ==================== Query Optimization ====================
    
    /**
     * Query with hints for performance optimization.
     */
    public List<AgentEntity> findActiveWithHints() {
        String jpql = "SELECT a FROM AgentEntity a WHERE a.status = true";
        TypedQuery<AgentEntity> query = entityManager.createQuery(jpql, AgentEntity.class);
        
        // Add query hints for optimization
        query.setHint("jakarta.persistence.query.timeout", 5000); // 5 seconds timeout
        query.setHint("jakarta.persistence.cache.retrieveMode", "USE");
        query.setHint("jakarta.persistence.cache.storeMode", "USE");
        
        return query.getResultList();
    }

    /**
     * Pagination with fetch size hint.
     */
    public List<AgentEntity> findAllPaginated(int page, int size) {
        String jpql = "SELECT a FROM AgentEntity a ORDER BY a.agentCode";
        TypedQuery<AgentEntity> query = entityManager.createQuery(jpql, AgentEntity.class);
        
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("agent-detail"));
        
        return query.getResultList();
    }

    // ==================== Helper Methods ====================
    
    /**
     * Check if agent exists.
     */
    public boolean existsByAgentCode(String agentCode) {
        String jpql = "SELECT COUNT(a) FROM AgentEntity a WHERE a.agentCode = :code";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("code", agentCode);
        return query.getSingleResult() > 0;
    }

    /**
     * Get all agents count.
     */
    public long countAll() {
        String jpql = "SELECT COUNT(a) FROM AgentEntity a";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    /**
     * Find all agents.
     */
    public List<AgentEntity> findAll() {
        String jpql = "SELECT a FROM AgentEntity a ORDER BY a.agentCode";
        return entityManager.createQuery(jpql, AgentEntity.class).getResultList();
    }

    /**
     * Dynamic query with filter support.
     * Handles any combination of branchCode, channel, and active filters.
     * Null parameters are ignored.
     */
    public List<AgentEntity> findByFilters(String branchCode, Channel channel, Boolean active) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AgentEntity> cq = cb.createQuery(AgentEntity.class);
        Root<AgentEntity> agent = cq.from(AgentEntity.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Add predicates only for non-null parameters
        if (branchCode != null) {
            predicates.add(cb.equal(agent.get("branchCode"), branchCode));
        }
        
        if (channel != null) {
            predicates.add(cb.equal(agent.get("channel"), channel));
        }
        
        if (active != null) {
            predicates.add(cb.equal(agent.get("active"), active));
        }
        
        // Apply all predicates with AND
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        cq.orderBy(cb.asc(agent.get("agentCode")));
        
        return entityManager.createQuery(cq).getResultList();
    }

    // ==================== Transaction Management ====================
    
    /**
     * Force flush to database.
     */
    @Transactional
    public void flush() {
        entityManager.flush();
    }

    /**
     * Clear persistence context.
     */
    public void clear() {
        entityManager.clear();
    }

    /**
     * Refresh entity from database.
     */
    @Transactional
    public void refresh(AgentEntity entity) {
        if (entityManager.contains(entity)) {
            entityManager.refresh(entity);
        }
    }

    /**
     * Detach entity from persistence context.
     */
    public void detach(AgentEntity entity) {
        if (entityManager.contains(entity)) {
            entityManager.detach(entity);
        }
    }

    /**
     * Check if entity is managed.
     */
    public boolean isManaged(AgentEntity entity) {
        return entityManager.contains(entity);
    }

    // ==================== Subqueries Example ====================
    
    /**
     * Find agents with subquery.
     * Example: Find agents in branches with more than 10 agents.
     */
    public List<AgentEntity> findInLargeBranches() {
        String jpql = "SELECT a FROM AgentEntity a " +
                     "WHERE a.branchCode IN " +
                     "(SELECT a2.branchCode FROM AgentEntity a2 " +
                     " GROUP BY a2.branchCode " +
                     " HAVING COUNT(a2) > 10) " +
                     "ORDER BY a.branchCode, a.name";
        
        return entityManager.createQuery(jpql, AgentEntity.class).getResultList();
    }

    /**
     * Find agents using EXISTS subquery.
     */
    public List<AgentEntity> findActiveAgentsInActiveBranches() {
        String jpql = "SELECT a FROM AgentEntity a " +
                     "WHERE a.status = true " +
                     "AND EXISTS (SELECT 1 FROM AgentEntity a2 " +
                     "            WHERE a2.branchCode = a.branchCode " +
                     "            AND a2.status = true) " +
                     "ORDER BY a.name";
        
        return entityManager.createQuery(jpql, AgentEntity.class).getResultList();
    }
}
