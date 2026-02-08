package io.jeecloud.aidigitalsignage.digitalsignage.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * Dashboard Metrics Aggregate Root
 * 
 * Domain entity representing key performance indicators.
 * Pure business logic - no framework dependencies.
 */
@Getter
@Builder
public class DashboardMetrics {

    private final Integer totalAudience;
    private final Integer totalViews;
    private final Integer totalAds;
    private final Double avgViewSeconds;
    private final AgeDistribution ageDistribution;
    private final GenderDistribution genderDistribution;
    private final EmotionDistribution emotionDistribution;

    /**
     * Domain model for age demographics
     */
    @Getter
    @Builder
    public static class AgeDistribution {
        private final Integer children;
        private final Integer teenagers;
        private final Integer youngAdults;
        private final Integer midAged;
        private final Integer seniors;
    }

    /**
     * Domain model for gender demographics
     */
    @Getter
    @Builder
    public static class GenderDistribution {
        private final Integer male;
        private final Integer female;
    }

    /**
     * Domain model for emotion analysis
     */
    @Getter
    @Builder
    public static class EmotionDistribution {
        private final Integer neutral;
        private final Integer serious;
        private final Integer happy;
        private final Integer surprised;
    }
}
