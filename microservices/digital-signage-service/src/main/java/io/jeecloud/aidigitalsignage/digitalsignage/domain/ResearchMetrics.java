package io.jeecloud.aidigitalsignage.digitalsignage.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * Research Metrics Aggregate Root
 * 
 * Domain entity representing research validation metrics.
 * Used to demonstrate:
 * - Face detection accuracy (>94% requirement)
 * - Gaze tracking quality
 * - Effectiveness vs baseline (static signage comparison)
 * 
 * Pure business logic - no framework dependencies.
 */
@Getter
@Builder
public class ResearchMetrics {

    private final FaceDetectionMetrics faceDetection;
    private final GazeQualityMetrics gazeQuality;
    private final ComparisonMetrics comparison;

    /**
     * Domain model for face detection accuracy metrics
     */
    @Getter
    @Builder
    public static class FaceDetectionMetrics {
        private final Double accuracy;           // Percentage (0-100)
        private final Double confidence;         // Average confidence (0-1)
        private final Integer framesProcessed;
        private final Integer facesDetected;

        /**
         * Check if face detection meets research requirement (>94%)
         */
        public boolean meetsResearchRequirement() {
            return accuracy != null && accuracy >= 94.0;
        }

        /**
         * Get quality rating based on accuracy
         */
        public String getQualityRating() {
            if (accuracy == null) return "UNKNOWN";
            if (accuracy >= 95.0) return "EXCELLENT";
            if (accuracy >= 90.0) return "GOOD";
            if (accuracy >= 85.0) return "ACCEPTABLE";
            return "POOR";
        }
    }

    /**
     * Domain model for gaze tracking quality metrics
     */
    @Getter
    @Builder
    public static class GazeQualityMetrics {
        private final Double primaryMethodRate;   // solvePnP percentage
        private final Double fallbackMethodRate;  // Pose estimation percentage
        private final Double avgConfidence;
        private final String recommendation;

        /**
         * Get quality score based on primary method usage
         */
        public String getQualityScore() {
            if (primaryMethodRate == null) return "UNKNOWN";
            if (primaryMethodRate >= 70.0) return "HIGH";
            if (primaryMethodRate >= 30.0) return "MEDIUM";
            return "LOW";
        }

        /**
         * Check if recalibration is recommended
         */
        public boolean needsRecalibration() {
            return primaryMethodRate != null && primaryMethodRate < 50.0;
        }
    }

    /**
     * Domain model for effectiveness comparison (static vs dynamic signage)
     */
    @Getter
    @Builder
    public static class ComparisonMetrics {
        private final BaselineData baseline;
        private final CurrentData current;
        private final ImprovementData improvement;

        @Getter
        @Builder
        public static class BaselineData {
            private final String condition;        // "static_signage"
            private final Double avgEngagement;
            private final String period;
        }

        @Getter
        @Builder
        public static class CurrentData {
            private final String condition;        // "dynamic_signage"
            private final Double avgEngagement;
            private final String period;
        }

        @Getter
        @Builder
        public static class ImprovementData {
            private final Double absolute;
            private final Double percentage;
            private final Boolean significant;

            /**
             * Check if improvement is meaningful for research (>50%)
             */
            public boolean isMeaningfulImprovement() {
                return percentage != null && percentage >= 50.0;
            }
        }

        /**
         * Calculate if comparison demonstrates system effectiveness
         */
        public boolean demonstratesEffectiveness() {
            return improvement != null && improvement.isMeaningfulImprovement();
        }
    }

    /**
     * Overall research validation status
     */
    public String getValidationStatus() {
        boolean accuracyMet = faceDetection != null && faceDetection.meetsResearchRequirement();
        boolean effectivenessMet = comparison != null && comparison.demonstratesEffectiveness();
        
        if (accuracyMet && effectivenessMet) {
            return "VALIDATED";
        } else if (accuracyMet || effectivenessMet) {
            return "PARTIALLY_VALIDATED";
        } else {
            return "VALIDATION_PENDING";
        }
    }
}
