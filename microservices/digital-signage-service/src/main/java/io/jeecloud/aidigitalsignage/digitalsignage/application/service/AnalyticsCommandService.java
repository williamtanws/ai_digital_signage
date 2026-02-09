package io.jeecloud.aidigitalsignage.digitalsignage.application.service;

import io.jeecloud.aidigitalsignage.digitalsignage.application.dto.UpdateAnalyticsRequest;
import io.jeecloud.aidigitalsignage.digitalsignage.application.port.in.UpdateAnalyticsUseCase;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.Advertisement;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.AdvertisementRepository;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.DashboardMetrics;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.DashboardMetricsRepository;
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
                .neutral(dto.getNeutral())
                .serious(dto.getSerious())
                .happy(dto.getHappy())
                .surprised(dto.getSurprised())
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
