package io.jeecloud.aidigitalsignage.digitalsignage.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Dashboard Overview Response DTO
 * 
 * Application layer data transfer object.
 * Used to transfer data between layers.
 */
@Getter
@Builder
@Schema(description = "Complete dashboard analytics overview including KPIs, demographics, emotions, and advertisement performance")
public class DashboardOverviewResponse {

    @Schema(description = "Total unique audience/viewers count", example = "57")
    private final Integer totalAudience;
    
    @Schema(description = "Total viewing sessions count", example = "57")
    private final Integer totalViews;
    
    @Schema(description = "Total advertisements tracked", example = "12")
    private final Integer totalAds;
    
    @Schema(description = "Average viewing time in seconds", example = "17.57")
    private final Double avgViewSeconds;
    
    @Schema(description = "Age distribution breakdown by categories")
    private final AgeDistributionDto ageDistribution;
    
    @Schema(description = "Gender distribution breakdown")
    private final GenderDistributionDto genderDistribution;
    
    @Schema(description = "Emotion detection distribution")
    private final EmotionDistributionDto emotionDistribution;
    
    @Schema(description = "Advertisement performance metrics per ad")
    private final List<AdsPerformanceDto> adsPerformance;
    
    @Schema(description = "Advertisement attention metrics per ad")
    private final List<AdsAttentionDto> adsAttention;
    
    @Schema(description = "System health and performance metrics for research validation")
    private final SystemHealthDto systemHealth;
    
    @Schema(description = "Research validation metrics (face detection accuracy, gaze quality, baseline comparison)")
    private final ResearchMetricsDto researchMetrics;
}
