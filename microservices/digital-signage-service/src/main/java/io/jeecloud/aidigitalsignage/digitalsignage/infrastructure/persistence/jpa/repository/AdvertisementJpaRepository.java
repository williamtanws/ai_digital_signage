package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository;

import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.AdvertisementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Advertisement JPA Repository (Infrastructure Layer)
 */
public interface AdvertisementJpaRepository extends JpaRepository<AdvertisementEntity, Long> {
    List<AdvertisementEntity> findAllByOrderByTotalViewersDesc();
}
