package io.jeecloud.aidigitalsignage.digitalsignage.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * Research Metrics DTO
 * 
 * Application layer DTO for research validation metrics.
 * Demonstrates face detection accuracy (>94%), gaze quality, and effectiveness vs baseline.
 */
@Getter
@Builder
@Schema(description = "Research validation metrics for academic publication")
public class ResearchMetricsDto {

    @Schema(description = "Face detection accuracy metrics")
    private final FaceDetectionMetricsDto faceDetection;
    
    @Schema(description = "Gaze tracking quality metrics")
    private final GazeQualityMetricsDto gazeQuality;
    
    @Schema(description = "Comparison with baseline (static signage)")
    private final ComparisonMetricsDto comparison;

    @Getter
    @Builder
    @Schema(description = "Face detection accuracy metrics")
    public static class FaceDetectionMetricsDto {
        @Schema(description = "Detection accuracy percentage", example = "95.6")
        private final Double accuracy;
        
        @Schema(description = "Average confidence score (0-1)", example = "0.92")
        private final Double confidence;
        
        @Schema(description = "Total frames processed", example = "1247")
        private final Integer framesProcessed;
        
        @Schema(description = "Total faces detected", example = "1192")
        private final Integer facesDetected;
        
        @Schema(description = "Quality rating (EXCELLENT, GOOD, ACCEPTABLE, POOR)", example = "EXCELLENT")
        private final String qualityRating;
    }

    @Getter
    @Builder
    @Schema(description = "Gaze tracking quality metrics")
    public static class GazeQualityMetricsDto {
        @Schema(description = "Primary method (solvePnP) usage percentage", example = "0.0")
        private final Double primaryMethodRate;
        
        @Schema(description = "Fallback method (pose estimation) usage percentage", example = "100.0")
        private final Double fallbackMethodRate;
        
        @Schema(description = "Average confidence score", example = "0.68")
        private final Double avgConfidence;
        
        @Schema(description = "Quality score (HIGH, MEDIUM, LOW)", example = "MEDIUM")
        private final String qualityScore;
        
        @Schema(description = "Recommendation for system improvement", example = "Consider recalibration")
        private final String recommendation;
    }

    @Getter
    @Builder
    @Schema(description = "Comparison metrics between static and dynamic signage")
    public static class ComparisonMetricsDto {
        @Schema(description = "Baseline (static signage) data")
        private final BaselineDataDto baseline;
        
        @Schema(description = "Current (dynamic signage) data")
        private final CurrentDataDto current;
        
        @Schema(description = "Improvement metrics")
        private final ImprovementDataDto improvement;

        @Getter
        @Builder
        @Schema(description = "Baseline data from static signage period")
        public static class BaselineDataDto {
            @Schema(description = "Experimental condition", example = "static_signage")
            private final String condition;
            
            @Schema(description = "Average engagement time in seconds", example = "8.3")
            private final Double avgEngagement;
            
            @Schema(description = "Time period", example = "2026-02-01 to 2026-02-07")
            private final String period;
        }

        @Getter
        @Builder
        @Schema(description = "Current data from dynamic signage period")
        public static class CurrentDataDto {
            @Schema(description = "Experimental condition", example = "dynamic_signage")
            private final String condition;
            
            @Schema(description = "Average engagement time in seconds", example = "25.4")
            private final Double avgEngagement;
            
            @Schema(description = "Time period", example = "2026-02-08 to 2026-02-14")
            private final String period;
        }

        @Getter
        @Builder
        @Schema(description = "Improvement metrics comparing static vs dynamic")
        public static class ImprovementDataDto {
            @Schema(description = "Absolute improvement in seconds", example = "17.1")
            private final Double absolute;
            
            @Schema(description = "Percentage improvement", example = "206.3")
            private final Double percentage;
            
            @Schema(description = "Whether improvement is statistically significant", example = "true")
            private final Boolean significant;
        }
    }
}
