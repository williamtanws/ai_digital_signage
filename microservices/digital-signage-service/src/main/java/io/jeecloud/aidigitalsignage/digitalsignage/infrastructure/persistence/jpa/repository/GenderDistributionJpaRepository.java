package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository;

import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.GenderDistributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Gender Distribution JPA Repository (Infrastructure Layer)
 */
public interface GenderDistributionJpaRepository extends JpaRepository<GenderDistributionEntity, Long> {
    Optional<GenderDistributionEntity> findFirstBy();
}
