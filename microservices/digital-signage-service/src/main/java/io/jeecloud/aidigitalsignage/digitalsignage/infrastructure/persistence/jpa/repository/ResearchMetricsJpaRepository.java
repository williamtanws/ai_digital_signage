package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository;

import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.ResearchMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Research Metrics JPA Repository (Infrastructure Layer)
 * 
 * Spring Data JPA repository for database operations.
 */
public interface ResearchMetricsJpaRepository extends JpaRepository<ResearchMetricsEntity, Long> {
    
    /**
     * Get the most recent research metrics record
     */
    @Query("SELECT r FROM ResearchMetricsEntity r ORDER BY r.lastUpdated DESC LIMIT 1")
    Optional<ResearchMetricsEntity> findMostRecent();
}
