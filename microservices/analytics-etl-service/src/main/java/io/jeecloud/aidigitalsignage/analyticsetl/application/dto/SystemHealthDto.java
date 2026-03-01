package io.jeecloud.aidigitalsignage.analyticsetl.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * System Health DTO
 * Matches the backend's SystemHealthDto for API compatibility
 */
@Data
@Builder
public class SystemHealthDto {
    private String status;
    private PerformanceMetricsDto performance;
    private EnvironmentMetricsDto environment;
    private Long uptime;
    
    @Data
    @Builder
    public static class PerformanceMetricsDto {
        private Double currentFps;
        private Double avgFps;
        private Double minFps;
        private Double maxFps;
        private Double currentCpuTemp;
        private Double maxCpuTemp;
        private Double cpuThreshold;
    }
    
    @Data
    @Builder
    public static class EnvironmentMetricsDto {
        private Double temperatureCelsius;
        private Double humidityPercent;
        private Double pressureHpa;
        private Double gasResistanceOhms;
        private Double noiseDb;
    }
}
