package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.adapter;

import io.jeecloud.aidigitalsignage.digitalsignage.domain.Advertisement;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.AdvertisementRepository;
import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.AdvertisementEntity;
import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository.AdvertisementJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Advertisement Repository Adapter (Infrastructure Layer)
 * 
 * Implements domain repository interface using JPA.
 * Translates between domain models and JPA entities.
 */
@Repository
@RequiredArgsConstructor
public class AdvertisementRepositoryAdapter implements AdvertisementRepository {

    private final AdvertisementJpaRepository jpaRepository;

    @Override
    public List<Advertisement> findAllOrderedByViewers() {
        return jpaRepository.findAllByOrderByTotalViewersDesc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Advertisement toDomain(AdvertisementEntity entity) {
        return Advertisement.builder()
                .id(entity.getId())
                .adName(entity.getAdName())
                .totalViewers(entity.getTotalViewers())
                .lookYes(entity.getLookYes())
                .lookNo(entity.getLookNo())
                .build();
    }
}
