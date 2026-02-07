package io.jeecloud.aidigitalsignage.digitalsignage.service;

import io.jeecloud.aidigitalsignage.digitalsignage.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Dashboard Service
 * 
 * Provides mock data for the dashboard overview.
 * 
 * This service generates realistic sample data representing:
 * - Audience demographics (age, gender)
 * - Emotional engagement (facial expressions)
 * - Advertisement performance metrics
 * - View and attention statistics
 * 
 * Purpose: Academic demonstration - no actual database or AI processing.
 * The mock data represents what would be collected by edge AI devices
 * analyzing audience behavior in a real deployment.
 */
@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    /**
     * Retrieves complete dashboard overview with all metrics.
     * 
     * Returns a comprehensive response including:
     * - KPIs: Total audience, views, ads, average view time
     * - Demographics: Age and gender distributions
     * - Emotions: Facial expression analysis
     * - Ad Performance: Viewer counts per advertisement
     * - Ad Attention: Engagement metrics per advertisement
     * 
     * @return Complete dashboard data as a single response object
     */
    public DashboardOverviewResponse getDashboardOverview() {
        log.debug("Generating mock dashboard overview data");

        return DashboardOverviewResponse.builder()
                .totalAudience(calculateTotalAudience())
                .totalViews(calculateTotalViews())
                .totalAds(calculateTotalAds())
                .avgViewSeconds(calculateAvgViewSeconds())
                .ageDistribution(generateAgeDistribution())
                .genderDistribution(generateGenderDistribution())
                .emotionDistribution(generateEmotionDistribution())
                .adsPerformance(generateAdsPerformance())
                .adsAttention(generateAdsAttention())
                .build();
    }

    // ========================================
    // KPI Calculations
    // ========================================

    /**
     * Calculates total unique audience members.
     * In production: Sum of unique face IDs detected across all sessions.
     */
    private Integer calculateTotalAudience() {
        return 1247; // Mock: ~1200 unique visitors
    }

    /**
     * Calculates total content views.
     * In production: Count of all viewing sessions recorded.
     */
    private Integer calculateTotalViews() {
        return 3856; // Mock: Multiple views per person
    }

    /**
     * Calculates total advertisements displayed.
     * In production: Count of distinct ads shown across all screens.
     */
    private Integer calculateTotalAds() {
        return 12; // Mock: 12 different advertisements
    }

    /**
     * Calculates average viewing duration.
     * In production: Mean of all session durations in seconds.
     */
    private Double calculateAvgViewSeconds() {
        return 24.5; // Mock: ~25 seconds average engagement
    }

    // ========================================
    // Demographic Data Generators
    // ========================================

    /**
     * Generates age distribution breakdown.
     * 
     * Mock data represents typical shopping mall demographics:
     * - Children: 12% (family outings)
     * - Teenagers: 18% (high engagement group)
     * - Young Adults: 35% (primary audience)
     * - Mid-Aged: 25% (purchasing power)
     * - Seniors: 10% (growing segment)
     */
    private AgeDistributionDto generateAgeDistribution() {
        return AgeDistributionDto.builder()
                .children(150)      // 12%
                .teenagers(225)     // 18%
                .youngAdults(437)   // 35%
                .midAged(312)       // 25%
                .seniors(123)       // 10%
                .build();
    }

    /**
     * Generates gender distribution breakdown.
     * 
     * Mock data represents balanced audience:
     * - Male: 52%
     * - Female: 48%
     */
    private GenderDistributionDto generateGenderDistribution() {
        return GenderDistributionDto.builder()
                .male(648)    // 52%
                .female(599)  // 48%
                .build();
    }

    /**
     * Generates emotion distribution breakdown.
     * 
     * Mock data based on typical public space behavior:
     * - Neutral: 45% (default expression)
     * - Serious: 25% (focused attention)
     * - Happy: 22% (positive engagement)
     * - Surprised: 8% (momentary reactions)
     */
    private EmotionDistributionDto generateEmotionDistribution() {
        return EmotionDistributionDto.builder()
                .neutral(561)    // 45%
                .serious(312)    // 25%
                .happy(274)      // 22%
                .surprised(100)  // 8%
                .build();
    }

    // ========================================
    // Advertisement Analytics Generators
    // ========================================

    /**
     * Generates advertisement performance data.
     * 
     * Mock data shows viewer counts for each advertisement.
     * Represents realistic variance in ad effectiveness:
     * - High performers: 400+ viewers
     * - Medium performers: 250-350 viewers
     * - Low performers: 150-200 viewers
     * 
     * @return List of performance metrics per advertisement
     */
    private List<AdsPerformanceDto> generateAdsPerformance() {
        return Arrays.asList(
                // High-performing ads (attractive content/placement)
                AdsPerformanceDto.builder()
                        .adName("Summer Sale 2026")
                        .totalViewers(485)
                        .build(),
                AdsPerformanceDto.builder()
                        .adName("New Product Launch")
                        .totalViewers(432)
                        .build(),
                
                // Medium-performing ads
                AdsPerformanceDto.builder()
                        .adName("Tech Gadgets")
                        .totalViewers(356)
                        .build(),
                AdsPerformanceDto.builder()
                        .adName("Fashion Collection")
                        .totalViewers(328)
                        .build(),
                AdsPerformanceDto.builder()
                        .adName("Food & Dining")
                        .totalViewers(295)
                        .build(),
                AdsPerformanceDto.builder()
                        .adName("Travel Packages")
                        .totalViewers(267)
                        .build(),
                
                // Lower-performing ads (less prominent or niche content)
                AdsPerformanceDto.builder()
                        .adName("Home Appliances")
                        .totalViewers(234)
                        .build(),
                AdsPerformanceDto.builder()
                        .adName("Sports Equipment")
                        .totalViewers(212)
                        .build(),
                AdsPerformanceDto.builder()
                        .adName("Beauty Products")
                        .totalViewers(198)
                        .build(),
                AdsPerformanceDto.builder()
                        .adName("Books & Media")
                        .totalViewers(187)
                        .build(),
                AdsPerformanceDto.builder()
                        .adName("Pet Supplies")
                        .totalViewers(156)
                        .build(),
                AdsPerformanceDto.builder()
                        .adName("Health Supplements")
                        .totalViewers(143)
                        .build()
        );
    }

    /**
     * Generates advertisement attention metrics.
     * 
     * Mock data shows engagement levels (looked vs. not looked).
     * Realistic attention patterns:
     * - High attention ads: 70-80% look rate
     * - Medium attention ads: 55-65% look rate
     * - Low attention ads: 40-50% look rate
     * 
     * Total looks + no-looks should roughly match total viewers.
     * 
     * @return List of attention metrics per advertisement
     */
    private List<AdsAttentionDto> generateAdsAttention() {
        return Arrays.asList(
                // High attention rates (engaging content)
                AdsAttentionDto.builder()
                        .adName("Summer Sale 2026")
                        .lookYes(388)  // 80% attention rate
                        .lookNo(97)
                        .build(),
                AdsAttentionDto.builder()
                        .adName("New Product Launch")
                        .lookYes(346)  // 80% attention rate
                        .lookNo(86)
                        .build(),
                
                // Medium attention rates
                AdsAttentionDto.builder()
                        .adName("Tech Gadgets")
                        .lookYes(227)  // 64% attention rate
                        .lookNo(129)
                        .build(),
                AdsAttentionDto.builder()
                        .adName("Fashion Collection")
                        .lookYes(197)  // 60% attention rate
                        .lookNo(131)
                        .build(),
                AdsAttentionDto.builder()
                        .adName("Food & Dining")
                        .lookYes(177)  // 60% attention rate
                        .lookNo(118)
                        .build(),
                AdsAttentionDto.builder()
                        .adName("Travel Packages")
                        .lookYes(160)  // 60% attention rate
                        .lookNo(107)
                        .build(),
                
                // Lower attention rates (less engaging or poor placement)
                AdsAttentionDto.builder()
                        .adName("Home Appliances")
                        .lookYes(117)  // 50% attention rate
                        .lookNo(117)
                        .build(),
                AdsAttentionDto.builder()
                        .adName("Sports Equipment")
                        .lookYes(106)  // 50% attention rate
                        .lookNo(106)
                        .build(),
                AdsAttentionDto.builder()
                        .adName("Beauty Products")
                        .lookYes(89)   // 45% attention rate
                        .lookNo(109)
                        .build(),
                AdsAttentionDto.builder()
                        .adName("Books & Media")
                        .lookYes(84)   // 45% attention rate
                        .lookNo(103)
                        .build(),
                AdsAttentionDto.builder()
                        .adName("Pet Supplies")
                        .lookYes(62)   // 40% attention rate
                        .lookNo(94)
                        .build(),
                AdsAttentionDto.builder()
                        .adName("Health Supplements")
                        .lookYes(57)   // 40% attention rate
                        .lookNo(86)
                        .build()
        );
    }
}
