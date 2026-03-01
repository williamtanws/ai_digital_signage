package io.jeecloud.aidigitalsignage.digitalsignage.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * System Health Aggregate Root
 * 
 * Domain entity representing system performance and operational metrics.
 * Used for research validation: demonstrates stable real-time performance (5-10 FPS).
 * Pure business logic - no framework dependencies.
 */
@Getter
@Builder
public class SystemHealth {

    private final String status;           // operational, throttled, overheated
    private final PerformanceMetrics performance;
    private final EnvironmentMetrics environment;
    private final String uptime;

    /**
     * Domain model for performance metrics (FPS, CPU temperature)
     */
    @Getter
    @Builder
    public static class PerformanceMetrics {
        private final Double currentFps;
        private final Double avgFps;
        private final Double minFps;
        private final Double maxFps;
        private final Double currentCpuTemp;
        private final Double maxCpuTemp;
        private final Double cpuThreshold;

        /**
         * Check if system is meeting performance requirements (5-10 FPS)
         */
        public boolean isPerformanceAdequate() {
            return currentFps != null && currentFps >= 5.0 && currentFps <= 10.0;
        }

        /**
         * Check if CPU is at risk of throttling (approaching 78°C)
         */
        public boolean isCpuAtRisk() {
            return currentCpuTemp != null && cpuThreshold != null 
                && currentCpuTemp >= (cpuThreshold * 0.9);
        }
    }

    /**
     * Domain model for environment context (temperature, humidity, noise)
     */
    @Getter
    @Builder
    public static class EnvironmentMetrics {
        private final Double temperatureCelsius;
        private final Double humidityPercent;
        private final Double pressureHpa;
        private final Double gasResistanceOhms;
        private final Double noiseDb;

        /**
         * Check if environment is within optimal operating conditions
         */
        public boolean isOptimalEnvironment() {
            return temperatureCelsius != null && temperatureCelsius >= 20.0 && temperatureCelsius <= 35.0
                && humidityPercent != null && humidityPercent >= 30.0 && humidityPercent <= 70.0;
        }
    }

    /**
     * Determine overall system status based on performance and environment
     */
    public String determineStatus() {
        if (performance.isCpuAtRisk()) {
            return "throttled";
        }
        if (!performance.isPerformanceAdequate()) {
            return "degraded";
        }
        if (environment != null && !environment.isOptimalEnvironment()) {
            return "suboptimal_environment";
        }
        return "operational";
    }
}
