package io.jeecloud.aidigitalsignage.digitalsignage.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for analytics update request from analytics-etl-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAnalyticsRequest {
    
    private DashboardMetricsDto dashboardMetrics;
    private List<AdMetricsDto> adMetrics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardMetricsDto {
        // KPI Metrics
        private Integer totalAudience;
        private Integer totalViews;
        private Integer totalAds;
        private Double avgViewSeconds;
        
        // Age Distribution
        private Integer children;
        private Integer teenagers;
        private Integer youngAdults;
        private Integer midAged;
        private Integer seniors;
        
        // Gender Distribution
        private Integer male;
        private Integer female;
        
        // Emotion Distribution
        private Integer neutral;
        private Integer serious;
        private Integer happy;
        private Integer surprised;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdMetricsDto {
        private String adName;
        private Integer totalViewers;
        private Integer lookYes;
        private Integer lookNo;
    }
}
