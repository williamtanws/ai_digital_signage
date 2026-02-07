package io.jeecloud.aidigitalsignage.agent.infrastructure.persistence;

import io.jeecloud.aidigitalsignage.agent.domain.*;
import io.jeecloud.aidigitalsignage.common.domain.AgentCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository adapter for Agent aggregate.
 * Implements the domain AgentRepository interface using a mix of JPA and EntityManager repositories.
 * 
 * <p>This adapter follows Hexagonal Architecture principles by:
 * <ul>
 *   <li>Implementing domain repository interface (port)</li>
 *   <li>Bridging domain layer with infrastructure (JPA persistence)</li>
 *   <li>Handling entity-domain model mapping</li>
 * </ul>
 * 
 * <p>Repository Strategy:
 * <ul>
 *   <li>JPA Repository: findById, findByBranchCode, findAllActive, delete operations</li>
 *   <li>EntityManager Repository: save, findByAgentCode, findByChannel, findAll, exists checks</li>
 * </ul>
 * 
 * <p>The mixed approach demonstrates flexibility in choosing the right tool:
 * <ul>
 *   <li>JPA for simple CRUD and queries</li>
 *   <li>EntityManager for custom operations and advanced JPA features</li>
 * </ul>
 * 
 * @see AgentRepository
 * @see AgentEntity
 * @see AgentEntityManagerRepository
 * @see AgentJpaRepository
 */
@Component
public class AgentRepositoryAdapter implements AgentRepository {
    
    // Mix of EntityManager and JPA repositories
    private final AgentEntityManagerRepository agentEntityManagerRepository;
    private final AgentJpaRepository agentJpaRepository;

    public AgentRepositoryAdapter(
            AgentEntityManagerRepository agentEntityManagerRepository,
            AgentJpaRepository agentJpaRepository
    ) {
        this.agentEntityManagerRepository = agentEntityManagerRepository;
        this.agentJpaRepository = agentJpaRepository;
    }

    @Override
    @Transactional
    public Agent save(Agent agent) {
        AgentEntity entity = toEntity(agent);
        // Use EntityManager Repository for save operations
        AgentEntity savedEntity = agentEntityManagerRepository.save(entity);
        Agent savedAgent = toDomain(savedEntity);
        
        // Note: AgentStatusChangedEvent is no longer published externally
        // One-way sync: User → Agent (not bidirectional)
        agent.clearDomainEvents();
        
        return savedAgent;
    }

    @Override
    public Optional<Agent> findById(AgentCode agentCode) {
        // Use JPA Repository for findById
        return agentJpaRepository.findById(agentCode.value())
            .map(this::toDomain);
    }

    @Override
    public Optional<Agent> findByAgentCode(AgentCode agentCode) {
        // Use EntityManager Repository for findByAgentCode
        return agentEntityManagerRepository.findByAgentCode(agentCode.value())
            .map(this::toDomain);
    }

    @Override
    public List<Agent> findByBranchCode(String branchCode) {
        // Use JPA Repository for findByBranchCode
        return agentJpaRepository.findByBranchCode(branchCode).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Agent> findByChannel(Channel channel) {
        // Use EntityManager Repository for findByChannel
        return agentEntityManagerRepository.findByChannel(channel).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Agent> findAllActive() {
        // Use JPA Repository for findAllActive
        return agentJpaRepository.findAllActive().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Agent> findAllInactive() {
        // Use JPA Repository for findAllInactive
        return agentJpaRepository.findAllInactive().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Agent> findByBranchCodeAndStatus(String branchCode, boolean status) {
        // Use JPA Repository for combined filter
        return agentJpaRepository.findByBranchCodeAndStatus(branchCode, status).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Agent> findByChannelAndStatus(Channel channel, boolean status) {
        // Use JPA Repository for combined filter
        return agentJpaRepository.findByChannelAndStatus(channel, status).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Agent> findAll() {
        // Use EntityManager Repository for findAll
        return agentEntityManagerRepository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(AgentCode agentCode) {
        // Use JPA Repository for delete
        agentJpaRepository.deleteById(agentCode.value());
    }

    @Override
    public boolean existsByAgentCode(AgentCode agentCode) {
        // Use EntityManager Repository for exists
        return agentEntityManagerRepository.existsByAgentCode(agentCode.value());
    }

    @Override
    public List<Agent> findByFilters(String branchCode, Channel channel, Boolean active) {
        // Use EntityManager Repository for dynamic filtering
        return agentEntityManagerRepository.findByFilters(branchCode, channel, active).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    // Mapping methods
    
    /**
     * Maps a domain agent to JPA entity.
     *
     * @param agent the domain agent
     * @return the JPA entity
     */
    private AgentEntity toEntity(Agent agent) {
        return AgentEntity.builder()
            .agentCode(agent.getAgentCode().value())
            .name(agent.getName())
            .branchCode(agent.getBranchCode())
            .channel(agent.getChannel())
            .status(agent.isStatus())
            .createDt(agent.getCreateDt())
            .updateDt(agent.getUpdateDt())
            .build();
    }

    /**
     * Maps a JPA entity to domain agent.
     *
     * @param entity the JPA entity
     * @return the domain agent
     */
    private Agent toDomain(AgentEntity entity) {
        return Agent.reconstitute(
            AgentCode.of(entity.getAgentCode()),
            entity.getName(),
            entity.getBranchCode(),
            entity.getChannel(),
            entity.isStatus(),
            entity.getCreateDt(),
            entity.getUpdateDt()
        );
    }
}

