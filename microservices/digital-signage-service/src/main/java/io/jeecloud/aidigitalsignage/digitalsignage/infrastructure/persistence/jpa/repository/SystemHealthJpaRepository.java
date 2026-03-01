package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository;

import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.SystemHealthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * System Health JPA Repository (Infrastructure Layer)
 * 
 * Spring Data JPA repository for database operations.
 */
public interface SystemHealthJpaRepository extends JpaRepository<SystemHealthEntity, Long> {
    
    /**
     * Get the most recent system health record
     */
    @Query("SELECT s FROM SystemHealthEntity s ORDER BY s.lastUpdated DESC LIMIT 1")
    Optional<SystemHealthEntity> findMostRecent();
}
