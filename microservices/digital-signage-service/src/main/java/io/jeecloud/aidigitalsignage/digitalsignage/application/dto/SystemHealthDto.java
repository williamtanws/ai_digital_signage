package io.jeecloud.aidigitalsignage.digitalsignage.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * System Health DTO
 * 
 * Application layer DTO for system performance and environment metrics.
 * Used for research validation of stable real-time performance (5-10 FPS).
 */
@Getter
@Builder
@Schema(description = "System performance and operational metrics for research validation")
public class SystemHealthDto {

    @Schema(description = "System status (operational, throttled, degraded)", example = "operational")
    private final String status;
    
    @Schema(description = "Performance metrics (FPS, CPU temperature)")
    private final PerformanceMetricsDto performance;
    
    @Schema(description = "Environment context (temperature, humidity, noise)")
    private final EnvironmentMetricsDto environment;
    
    @Schema(description = "System uptime", example = "48h 23m")
    private final String uptime;

    @Getter
    @Builder
    @Schema(description = "Performance metrics for FPS and CPU tracking")
    public static class PerformanceMetricsDto {
        @Schema(description = "Current frames per second", example = "9.2")
        private final Double currentFps;
        
        @Schema(description = "Average FPS", example = "9.1")
        private final Double avgFps;
        
        @Schema(description = "Minimum FPS recorded", example = "8.5")
        private final Double minFps;
        
        @Schema(description = "Maximum FPS recorded", example = "10.0")
        private final Double maxFps;
        
        @Schema(description = "Current CPU temperature in Celsius", example = "58.4")
        private final Double currentCpuTemp;
        
        @Schema(description = "Maximum CPU temperature recorded", example = "59.5")
        private final Double maxCpuTemp;
        
        @Schema(description = "CPU throttling threshold", example = "78.0")
        private final Double cpuThreshold;
    }

    @Getter
    @Builder
    @Schema(description = "Environment context metrics")
    public static class EnvironmentMetricsDto {
        @Schema(description = "Temperature in Celsius", example = "31.69")
        private final Double temperatureCelsius;
        
        @Schema(description = "Humidity percentage", example = "53.66")
        private final Double humidityPercent;
        
        @Schema(description = "Air pressure in hPa", example = "1002.43")
        private final Double pressureHpa;
        
        @Schema(description = "Gas resistance in Ohms", example = "42780.75")
        private final Double gasResistanceOhms;
        
        @Schema(description = "Noise level in dB", example = "57.8")
        private final Double noiseDb;
    }
}
