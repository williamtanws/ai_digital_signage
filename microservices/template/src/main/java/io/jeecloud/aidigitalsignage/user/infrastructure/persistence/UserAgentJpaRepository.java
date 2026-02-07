package io.jeecloud.aidigitalsignage.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for UserAgentEntity.
 * Manages the many-to-many relationship between Users and Agents.
 */
@Repository
public interface UserAgentJpaRepository extends JpaRepository<UserAgentEntity, UserAgentEntity.UserAgentId> {
    
    @Query("SELECT ua FROM UserAgentEntity ua WHERE ua.id.userId = :userId")
    List<UserAgentEntity> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT ua FROM UserAgentEntity ua WHERE ua.id.agentCode = :agentCode")
    List<UserAgentEntity> findByAgentCode(@Param("agentCode") String agentCode);
    
    @Modifying
    @Query("DELETE FROM UserAgentEntity ua WHERE ua.id.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}
