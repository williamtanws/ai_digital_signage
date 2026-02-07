package io.jeecloud.aidigitalsignage.digitalsignage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dashboard Overview Response DTO
 * 
 * Comprehensive response containing all dashboard metrics and analytics.
 * This single response includes:
 * - Top-level KPIs (audience, views, ads, average view time)
 * - Demographic breakdowns (age, gender)
 * - Emotional analysis
 * - Advertisement performance metrics
 * 
 * Used by the frontend to render the complete dashboard in one API call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {

    // === TOP-LEVEL KPIs ===
    
    /**
     * Total number of unique audience members detected
     */
    private Integer totalAudience;
    
    /**
     * Total number of content views recorded
     */
    private Integer totalViews;
    
    /**
     * Total number of advertisements displayed
     */
    private Integer totalAds;
    
    /**
     * Average viewing duration in seconds
     */
    private Double avgViewSeconds;

    // === DEMOGRAPHIC ANALYTICS ===
    
    /**
     * Age distribution breakdown
     */
    private AgeDistributionDto ageDistribution;
    
    /**
     * Gender distribution breakdown
     */
    private GenderDistributionDto genderDistribution;

    // === EMOTIONAL ANALYTICS ===
    
    /**
     * Emotion distribution breakdown
     */
    private EmotionDistributionDto emotionDistribution;

    // === ADVERTISEMENT ANALYTICS ===
    
    /**
     * Performance metrics per advertisement (viewer counts)
     */
    private List<AdsPerformanceDto> adsPerformance;
    
    /**
     * Attention metrics per advertisement (look yes/no counts)
     */
    private List<AdsAttentionDto> adsAttention;
}
