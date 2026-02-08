package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository;

import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.AgeDistributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Age Distribution JPA Repository (Infrastructure Layer)
 */
public interface AgeDistributionJpaRepository extends JpaRepository<AgeDistributionEntity, Long> {
    Optional<AgeDistributionEntity> findFirstBy();
}
