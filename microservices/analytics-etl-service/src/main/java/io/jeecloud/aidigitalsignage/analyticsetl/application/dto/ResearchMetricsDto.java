package io.jeecloud.aidigitalsignage.analyticsetl.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Research Metrics DTO
 * Matches the backend's ResearchMetricsDto for API compatibility
 */
@Data
@Builder
public class ResearchMetricsDto {
    private FaceDetectionMetricsDto faceDetection;
    private GazeQualityMetricsDto gazeQuality;
    private ComparisonMetricsDto comparison;
    
    @Data
    @Builder
    public static class FaceDetectionMetricsDto {
        private Double accuracy;
        private Double confidence;
        private Integer framesProcessed;
        private Integer facesDetected;
    }
    
    @Data
    @Builder
    public static class GazeQualityMetricsDto {
        private Double kptsValidPercent;
        private Double solvepnpSuccessPercent;
        private Double fallbackPercent;
    }
    
    @Data
    @Builder
    public static class ComparisonMetricsDto {
        private BaselineDataDto baseline;
        private CurrentDataDto current;
        private ImprovementDataDto improvement;
        
        @Data
        @Builder
        public static class BaselineDataDto {
            private String condition;
            private Double avgEngagement;
            private String period;
        }
        
        @Data
        @Builder
        public static class CurrentDataDto {
            private String condition;
            private Double avgEngagement;
            private String period;
        }
        
        @Data
        @Builder
        public static class ImprovementDataDto {
            private Double absolute;
            private Double percentage;
            private Boolean significant;
        }
    }
}
