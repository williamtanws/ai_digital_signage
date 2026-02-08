package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository;

import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.MetricsKpiEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Metrics KPI JPA Repository (Infrastructure Layer)
 * 
 * Spring Data JPA repository for database operations.
 */
public interface MetricsKpiJpaRepository extends JpaRepository<MetricsKpiEntity, Long> {
    Optional<MetricsKpiEntity> findFirstBy();
}
