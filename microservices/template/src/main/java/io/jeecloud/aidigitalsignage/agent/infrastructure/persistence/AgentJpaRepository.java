package io.jeecloud.aidigitalsignage.agent.infrastructure.persistence;

import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for AgentEntity.
 * This provides the actual database operations.
 */
@Repository
public interface AgentJpaRepository extends JpaRepository<AgentEntity, String> {
    
    Optional<AgentEntity> findByAgentCode(String agentCode);
    
    List<AgentEntity> findByBranchCode(String branchCode);
    
    List<AgentEntity> findByChannel(Channel channel);
    
    @Query("SELECT a FROM AgentEntity a WHERE a.status = true")
    List<AgentEntity> findAllActive();
    
    @Query("SELECT a FROM AgentEntity a WHERE a.status = false")
    List<AgentEntity> findAllInactive();
    
    List<AgentEntity> findByBranchCodeAndStatus(String branchCode, boolean status);
    
    List<AgentEntity> findByChannelAndStatus(Channel channel, boolean status);
    
    boolean existsByAgentCode(String agentCode);
}

