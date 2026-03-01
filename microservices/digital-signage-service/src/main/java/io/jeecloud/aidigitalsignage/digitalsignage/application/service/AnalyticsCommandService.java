package io.jeecloud.aidigitalsignage.digitalsignage.application.service;

import io.jeecloud.aidigitalsignage.digitalsignage.application.dto.SystemHealthDto;
import io.jeecloud.aidigitalsignage.digitalsignage.application.dto.ResearchMetricsDto;
import io.jeecloud.aidigitalsignage.digitalsignage.application.dto.UpdateAnalyticsRequest;
import io.jeecloud.aidigitalsignage.digitalsignage.application.port.in.UpdateAnalyticsUseCase;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Analytics Command Service (Application Layer - Command Side)
 * 
 * Handles write operations for analytics data.
 * Receives data from analytics-etl-service via REST API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsCommandService implements UpdateAnalyticsUseCase {
    
    private final DashboardMetricsRepository dashboardMetricsRepository;
    private final AdvertisementRepository advertisementRepository;
    private final SystemHealthRepository systemHealthRepository;
    private final ResearchMetricsRepository researchMetricsRepository;
    
    /**
     * Update all analytics data (clear and replace)
     * 
     * This is called by the analytics-etl-service after processing
     * gaze events from TDengine.
     */
    @Override
    @Transactional
    public void updateAnalytics(UpdateAnalyticsRequest request) {
        log.info("Received analytics update request from ETL service");
        
        try {
            // Step 1: Clear existing data
            log.debug("Clearing existing dashboard metrics");
            dashboardMetricsRepository.deleteAll();
            
            log.debug("Clearing existing advertisements");
            advertisementRepository.deleteAll();
            
            // Step 2: Save new dashboard metrics
            DashboardMetrics dashboardMetrics = mapToDashboardMetrics(request.getDashboardMetrics());
            dashboardMetricsRepository.save(dashboardMetrics);
            log.debug("Saved dashboard metrics");
            
            // Step 3: Save new advertisement data
            List<Advertisement> advertisements = request.getAdMetrics().stream()
                    .map(this::mapToAdvertisement)
                    .collect(Collectors.toList());
            
            advertisementRepository.saveAll(advertisements);
            
            // Step 4: Save system health (if provided)
            if (request.getSystemHealth() != null) {
                SystemHealth systemHealth = mapToSystemHealth(request.getSystemHealth());
                systemHealthRepository.save(systemHealth);
                log.debug("Saved system health metrics");
            }
            
            // Step 5: Save research metrics (if provided)
            if (request.getResearchMetrics() != null) {
                ResearchMetrics researchMetrics = mapToResearchMetrics(request.getResearchMetrics());
                researchMetricsRepository.save(researchMetrics);
                log.debug("Saved research metrics");
            }
            
            log.info("Successfully updated analytics: {} dashboard metrics, {} ads", 
                    1, advertisements.size());
            
        } catch (Exception e) {
            log.error("Failed to update analytics", e);
            throw new RuntimeException("Analytics update failed", e);
        }
    }
    
    /**
     * Map DTO to domain entity
     */
    private DashboardMetrics mapToDashboardMetrics(UpdateAnalyticsRequest.DashboardMetricsDto dto) {
        DashboardMetrics.AgeDistribution ageDistribution = DashboardMetrics.AgeDistribution.builder()
                .children(dto.getChildren())
                .teenagers(dto.getTeenagers())
                .youngAdults(dto.getYoungAdults())
                .midAged(dto.getMidAged())
                .seniors(dto.getSeniors())
                .build();
        
        DashboardMetrics.GenderDistribution genderDistribution = DashboardMetrics.GenderDistribution.builder()
                .male(dto.getMale())
                .female(dto.getFemale())
                .build();
        
        DashboardMetrics.EmotionDistribution emotionDistribution = DashboardMetrics.EmotionDistribution.builder()
                .anger(dto.getAnger())
                .contempt(dto.getContempt())
                .disgust(dto.getDisgust())
                .fear(dto.getFear())
                .happiness(dto.getHappiness())
                .neutral(dto.getNeutral())
                .sadness(dto.getSadness())
                .surprise(dto.getSurprise())
                .build();
        
        return DashboardMetrics.builder()
                .totalAudience(dto.getTotalAudience())
                .totalViews(dto.getTotalViews())
                .totalAds(dto.getTotalAds())
                .avgViewSeconds(dto.getAvgViewSeconds())
                .ageDistribution(ageDistribution)
                .genderDistribution(genderDistribution)
                .emotionDistribution(emotionDistribution)
                .build();
    }
    
    /**
     * Map SystemHealthDto to domain entity
     */
    private SystemHealth mapToSystemHealth(SystemHealthDto dto) {
        return SystemHealth.builder()
                .status(dto.getStatus())
                .performance(mapToPerformanceMetrics(dto.getPerformance()))
                .environment(mapToEnvironmentMetrics(dto.getEnvironment()))
                .uptime(dto.getUptime())
                .build();
    }
    
    private SystemHealth.PerformanceMetrics mapToPerformanceMetrics(SystemHealthDto.PerformanceMetricsDto dto) {
        if (dto == null) return null;
        return SystemHealth.PerformanceMetrics.builder()
                .currentFps(dto.getCurrentFps())
                .avgFps(dto.getAvgFps())
                .minFps(dto.getMinFps())
                .maxFps(dto.getMaxFps())
                .currentCpuTemp(dto.getCurrentCpuTemp())
                .maxCpuTemp(dto.getMaxCpuTemp())
                .cpuThreshold(dto.getCpuThreshold())
                .build();
    }
    
    private SystemHealth.EnvironmentMetrics mapToEnvironmentMetrics(SystemHealthDto.EnvironmentMetricsDto dto) {
        if (dto == null) return null;
        return SystemHealth.EnvironmentMetrics.builder()
                .temperatureCelsius(dto.getTemperatureCelsius())
                .humidityPercent(dto.getHumidityPercent())
                .pressureHpa(dto.getPressureHpa())
                .gasResistanceOhms(dto.getGasResistanceOhms())
                .noiseDb(dto.getNoiseDb())
                .build();
    }
    
    /**
     * Map ResearchMetricsDto to domain entity
     */
    private ResearchMetrics mapToResearchMetrics(ResearchMetricsDto dto) {
        return ResearchMetrics.builder()
                .faceDetection(mapToFaceDetection(dto.getFaceDetection()))
                .gazeQuality(mapToGazeQuality(dto.getGazeQuality()))
                .comparison(mapToComparison(dto.getComparison()))
                .build();
    }
    
    private ResearchMetrics.FaceDetectionMetrics mapToFaceDetection(ResearchMetricsDto.FaceDetectionMetricsDto dto) {
        if (dto == null) return null;
        return ResearchMetrics.FaceDetectionMetrics.builder()
                .accuracy(dto.getAccuracy())
                .confidence(dto.getConfidence())
                .framesProcessed(dto.getFramesProcessed())
                .facesDetected(dto.getFacesDetected())
                .build();
    }
    
    private ResearchMetrics.GazeQualityMetrics mapToGazeQuality(ResearchMetricsDto.GazeQualityMetricsDto dto) {
        if (dto == null) return null;
        return ResearchMetrics.GazeQualityMetrics.builder()
                .primaryMethodRate(dto.getPrimaryMethodRate())
                .fallbackMethodRate(dto.getFallbackMethodRate())
                .avgConfidence(dto.getAvgConfidence())
                .recommendation(dto.getRecommendation())
                .build();
    }
    
    private ResearchMetrics.ComparisonMetrics mapToComparison(ResearchMetricsDto.ComparisonMetricsDto dto) {
        if (dto == null) return null;
        return ResearchMetrics.ComparisonMetrics.builder()
                .baseline(mapToBaselineData(dto.getBaseline()))
                .current(mapToCurrentData(dto.getCurrent()))
                .improvement(mapToImprovementData(dto.getImprovement()))
                .build();
    }
    
    private ResearchMetrics.ComparisonMetrics.BaselineData mapToBaselineData(ResearchMetricsDto.ComparisonMetricsDto.BaselineDataDto dto) {
        if (dto == null) return null;
        return ResearchMetrics.ComparisonMetrics.BaselineData.builder()
                .condition(dto.getCondition())
                .avgEngagement(dto.getAvgEngagement())
                .period(dto.getPeriod())
                .build();
    }
    
    private ResearchMetrics.ComparisonMetrics.CurrentData mapToCurrentData(ResearchMetricsDto.ComparisonMetricsDto.CurrentDataDto dto) {
        if (dto == null) return null;
        return ResearchMetrics.ComparisonMetrics.CurrentData.builder()
                .condition(dto.getCondition())
                .avgEngagement(dto.getAvgEngagement())
                .period(dto.getPeriod())
                .build();
    }
    
    private ResearchMetrics.ComparisonMetrics.ImprovementData mapToImprovementData(ResearchMetricsDto.ComparisonMetricsDto.ImprovementDataDto dto) {
        if (dto == null) return null;
        return ResearchMetrics.ComparisonMetrics.ImprovementData.builder()
                .absolute(dto.getAbsolute())
                .percentage(dto.getPercentage())
                .significant(dto.getSignificant())
                .build();
    }
    
    /**
     * Map DTO to domain entity
     */
    private Advertisement mapToAdvertisement(UpdateAnalyticsRequest.AdMetricsDto dto) {
        return Advertisement.builder()
                .adName(dto.getAdName())
                .totalViewers(dto.getTotalViewers())
                .lookYes(dto.getLookYes())
                .lookNo(dto.getLookNo())
                .build();
    }
}
