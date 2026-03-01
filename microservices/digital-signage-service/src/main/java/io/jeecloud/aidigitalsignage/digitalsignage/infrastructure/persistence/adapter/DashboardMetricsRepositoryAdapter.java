package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.adapter;

import io.jeecloud.aidigitalsignage.digitalsignage.domain.DashboardMetrics;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.DashboardMetricsRepository;
import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.*;
import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Dashboard Metrics Repository Adapter (Infrastructure Layer)
 * 
 * Implements domain repository interface using JPA.
 * Translates between domain models and JPA entities.
 */
@Repository
@RequiredArgsConstructor
public class DashboardMetricsRepositoryAdapter implements DashboardMetricsRepository {

    private final MetricsKpiJpaRepository metricsKpiJpaRepository;
    private final AgeDistributionJpaRepository ageDistributionJpaRepository;
    private final GenderDistributionJpaRepository genderDistributionJpaRepository;
    private final EmotionDistributionJpaRepository emotionDistributionJpaRepository;

    @Override
    public Optional<DashboardMetrics> findCurrent() {
        Optional<MetricsKpiEntity> kpiEntity = metricsKpiJpaRepository.findFirstBy();
        
        if (kpiEntity.isEmpty()) {
            return Optional.empty();
        }

        Optional<AgeDistributionEntity> ageEntity = ageDistributionJpaRepository.findFirstBy();
        Optional<GenderDistributionEntity> genderEntity = genderDistributionJpaRepository.findFirstBy();
        Optional<EmotionDistributionEntity> emotionEntity = emotionDistributionJpaRepository.findFirstBy();

        if (ageEntity.isEmpty() || genderEntity.isEmpty() || emotionEntity.isEmpty()) {
            return Optional.empty();
        }

        DashboardMetrics metrics = DashboardMetrics.builder()
                .totalAudience(kpiEntity.get().getTotalAudience())
                .totalViews(kpiEntity.get().getTotalViews())
                .totalAds(kpiEntity.get().getTotalAds())
                .avgViewSeconds(kpiEntity.get().getAvgViewSeconds())
                .ageDistribution(mapAgeDistribution(ageEntity.get()))
                .genderDistribution(mapGenderDistribution(genderEntity.get()))
                .emotionDistribution(mapEmotionDistribution(emotionEntity.get()))
                .build();

        return Optional.of(metrics);
    }
    
    @Override
    public DashboardMetrics save(DashboardMetrics metrics) {
        // Save KPI metrics
        MetricsKpiEntity kpiEntity = new MetricsKpiEntity();
        kpiEntity.setTotalAudience(metrics.getTotalAudience());
        kpiEntity.setTotalViews(metrics.getTotalViews());
        kpiEntity.setTotalAds(metrics.getTotalAds());
        kpiEntity.setAvgViewSeconds(metrics.getAvgViewSeconds());
        metricsKpiJpaRepository.save(kpiEntity);
        
        // Save age distribution
        AgeDistributionEntity ageEntity = new AgeDistributionEntity();
        ageEntity.setChildren(metrics.getAgeDistribution().getChildren());
        ageEntity.setTeenagers(metrics.getAgeDistribution().getTeenagers());
        ageEntity.setYoungAdults(metrics.getAgeDistribution().getYoungAdults());
        ageEntity.setMidAged(metrics.getAgeDistribution().getMidAged());
        ageEntity.setSeniors(metrics.getAgeDistribution().getSeniors());
        ageDistributionJpaRepository.save(ageEntity);
        
        // Save gender distribution
        GenderDistributionEntity genderEntity = new GenderDistributionEntity();
        genderEntity.setMale(metrics.getGenderDistribution().getMale());
        genderEntity.setFemale(metrics.getGenderDistribution().getFemale());
        genderDistributionJpaRepository.save(genderEntity);
        
        // Save emotion distribution
        EmotionDistributionEntity emotionEntity = new EmotionDistributionEntity();
        emotionEntity.setAnger(metrics.getEmotionDistribution().getAnger());
        emotionEntity.setContempt(metrics.getEmotionDistribution().getContempt());
        emotionEntity.setDisgust(metrics.getEmotionDistribution().getDisgust());
        emotionEntity.setFear(metrics.getEmotionDistribution().getFear());
        emotionEntity.setHappiness(metrics.getEmotionDistribution().getHappiness());
        emotionEntity.setNeutral(metrics.getEmotionDistribution().getNeutral());
        emotionEntity.setSadness(metrics.getEmotionDistribution().getSadness());
        emotionEntity.setSurprise(metrics.getEmotionDistribution().getSurprise());
        emotionDistributionJpaRepository.save(emotionEntity);
        
        return metrics;
    }
    
    @Override
    public void deleteAll() {
        metricsKpiJpaRepository.deleteAllInBatch();
        ageDistributionJpaRepository.deleteAllInBatch();
        genderDistributionJpaRepository.deleteAllInBatch();
        emotionDistributionJpaRepository.deleteAllInBatch();
        metricsKpiJpaRepository.flush();
    }

    private DashboardMetrics.AgeDistribution mapAgeDistribution(AgeDistributionEntity entity) {
        return DashboardMetrics.AgeDistribution.builder()
                .children(entity.getChildren())
                .teenagers(entity.getTeenagers())
                .youngAdults(entity.getYoungAdults())
                .midAged(entity.getMidAged())
                .seniors(entity.getSeniors())
                .build();
    }

    private DashboardMetrics.GenderDistribution mapGenderDistribution(GenderDistributionEntity entity) {
        return DashboardMetrics.GenderDistribution.builder()
                .male(entity.getMale())
                .female(entity.getFemale())
                .build();
    }

    private DashboardMetrics.EmotionDistribution mapEmotionDistribution(EmotionDistributionEntity entity) {
        return DashboardMetrics.EmotionDistribution.builder()
                .anger(entity.getAnger())
                .contempt(entity.getContempt())
                .disgust(entity.getDisgust())
                .fear(entity.getFear())
                .happiness(entity.getHappiness())
                .neutral(entity.getNeutral())
                .sadness(entity.getSadness())
                .surprise(entity.getSurprise())
                .build();
    }
}
