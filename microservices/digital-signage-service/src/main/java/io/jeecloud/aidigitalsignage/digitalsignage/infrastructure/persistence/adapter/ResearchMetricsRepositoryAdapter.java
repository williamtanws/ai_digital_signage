package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.adapter;

import io.jeecloud.aidigitalsignage.digitalsignage.domain.ResearchMetrics;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.ResearchMetricsRepository;
import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.ResearchMetricsEntity;
import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository.ResearchMetricsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Research Metrics Repository Adapter (Infrastructure Layer)
 * 
 * Implements domain repository interface using JPA.
 * Translates between domain models and JPA entities.
 */
@Repository
@RequiredArgsConstructor
public class ResearchMetricsRepositoryAdapter implements ResearchMetricsRepository {

    private final ResearchMetricsJpaRepository jpaRepository;

    @Override
    public Optional<ResearchMetrics> findCurrent() {
        return jpaRepository.findMostRecent().map(this::toDomain);
    }

    @Override
    public ResearchMetrics save(ResearchMetrics researchMetrics) {
        ResearchMetricsEntity entity = toEntity(researchMetrics);
        ResearchMetricsEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void saveBaseline(Double avgEngagement, String period) {
        Optional<ResearchMetricsEntity> existing = jpaRepository.findMostRecent();
        ResearchMetricsEntity entity = existing.orElse(new ResearchMetricsEntity());
        
        entity.setBaselineCondition("static_signage");
        entity.setBaselineAvgEngagement(avgEngagement);
        entity.setBaselinePeriod(period);
        
        jpaRepository.save(entity);
    }

    private ResearchMetrics toDomain(ResearchMetricsEntity entity) {
        return ResearchMetrics.builder()
                .faceDetection(mapFaceDetection(entity))
                .gazeQuality(mapGazeQuality(entity))
                .comparison(mapComparison(entity))
                .build();
    }

    private ResearchMetrics.FaceDetectionMetrics mapFaceDetection(ResearchMetricsEntity entity) {
        if (entity.getFaceDetectionAccuracy() == null) {
            return null;
        }
        return ResearchMetrics.FaceDetectionMetrics.builder()
                .accuracy(entity.getFaceDetectionAccuracy())
                .confidence(entity.getFaceDetectionConfidence())
                .framesProcessed(entity.getFramesProcessed())
                .facesDetected(entity.getFacesDetected())
                .build();
    }

    private ResearchMetrics.GazeQualityMetrics mapGazeQuality(ResearchMetricsEntity entity) {
        if (entity.getPrimaryMethodRate() == null) {
            return null;
        }
        return ResearchMetrics.GazeQualityMetrics.builder()
                .primaryMethodRate(entity.getPrimaryMethodRate())
                .fallbackMethodRate(entity.getFallbackMethodRate())
                .avgConfidence(entity.getGazeAvgConfidence())
                .recommendation(entity.getGazeRecommendation())
                .build();
    }

    private ResearchMetrics.ComparisonMetrics mapComparison(ResearchMetricsEntity entity) {
        if (entity.getBaselineAvgEngagement() == null || entity.getCurrentAvgEngagement() == null) {
            return null;
        }

        return ResearchMetrics.ComparisonMetrics.builder()
                .baseline(ResearchMetrics.ComparisonMetrics.BaselineData.builder()
                        .condition(entity.getBaselineCondition())
                        .avgEngagement(entity.getBaselineAvgEngagement())
                        .period(entity.getBaselinePeriod())
                        .build())
                .current(ResearchMetrics.ComparisonMetrics.CurrentData.builder()
                        .condition(entity.getCurrentCondition())
                        .avgEngagement(entity.getCurrentAvgEngagement())
                        .period(entity.getCurrentPeriod())
                        .build())
                .improvement(ResearchMetrics.ComparisonMetrics.ImprovementData.builder()
                        .absolute(entity.getImprovementAbsolute())
                        .percentage(entity.getImprovementPercentage())
                        .significant(entity.getImprovementSignificant())
                        .build())
                .build();
    }

    private ResearchMetricsEntity toEntity(ResearchMetrics domain) {
        ResearchMetricsEntity entity = new ResearchMetricsEntity();
        
        if (domain.getFaceDetection() != null) {
            entity.setFaceDetectionAccuracy(domain.getFaceDetection().getAccuracy());
            entity.setFaceDetectionConfidence(domain.getFaceDetection().getConfidence());
            entity.setFramesProcessed(domain.getFaceDetection().getFramesProcessed());
            entity.setFacesDetected(domain.getFaceDetection().getFacesDetected());
        }
        
        if (domain.getGazeQuality() != null) {
            entity.setPrimaryMethodRate(domain.getGazeQuality().getPrimaryMethodRate());
            entity.setFallbackMethodRate(domain.getGazeQuality().getFallbackMethodRate());
            entity.setGazeAvgConfidence(domain.getGazeQuality().getAvgConfidence());
            entity.setGazeQualityScore(domain.getGazeQuality().getQualityScore());
            entity.setGazeRecommendation(domain.getGazeQuality().getRecommendation());
        }
        
        if (domain.getComparison() != null) {
            if (domain.getComparison().getBaseline() != null) {
                entity.setBaselineCondition(domain.getComparison().getBaseline().getCondition());
                entity.setBaselineAvgEngagement(domain.getComparison().getBaseline().getAvgEngagement());
                entity.setBaselinePeriod(domain.getComparison().getBaseline().getPeriod());
            }
            
            if (domain.getComparison().getCurrent() != null) {
                entity.setCurrentCondition(domain.getComparison().getCurrent().getCondition());
                entity.setCurrentAvgEngagement(domain.getComparison().getCurrent().getAvgEngagement());
                entity.setCurrentPeriod(domain.getComparison().getCurrent().getPeriod());
            }
            
            if (domain.getComparison().getImprovement() != null) {
                entity.setImprovementAbsolute(domain.getComparison().getImprovement().getAbsolute());
                entity.setImprovementPercentage(domain.getComparison().getImprovement().getPercentage());
                entity.setImprovementSignificant(domain.getComparison().getImprovement().getSignificant());
            }
        }
        
        return entity;
    }
}
