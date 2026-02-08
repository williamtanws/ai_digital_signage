package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository;

import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.EmotionDistributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Emotion Distribution JPA Repository (Infrastructure Layer)
 */
public interface EmotionDistributionJpaRepository extends JpaRepository<EmotionDistributionEntity, Long> {
    Optional<EmotionDistributionEntity> findFirstBy();
}
