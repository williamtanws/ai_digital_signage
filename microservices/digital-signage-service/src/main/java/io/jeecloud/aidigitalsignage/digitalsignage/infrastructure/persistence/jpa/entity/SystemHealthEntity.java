package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * System Health JPA Entity (Infrastructure Layer)
 * 
 * Persistence model for system performance and environment metrics.
 * Contains JPA annotations for database mapping.
 */
@Entity
@Table(name = "system_health")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemHealthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    // Performance metrics
    @Column(name = "current_fps")
    private Double currentFps;

    @Column(name = "avg_fps")
    private Double avgFps;

    @Column(name = "min_fps")
    private Double minFps;

    @Column(name = "max_fps")
    private Double maxFps;

    @Column(name = "current_cpu_temp")
    private Double currentCpuTemp;

    @Column(name = "max_cpu_temp")
    private Double maxCpuTemp;

    @Column(name = "cpu_threshold")
    private Double cpuThreshold;

    // Environment metrics
    @Column(name = "temperature_celsius")
    private Double temperatureCelsius;

    @Column(name = "humidity_percent")
    private Double humidityPercent;

    @Column(name = "pressure_hpa")
    private Double pressureHpa;

    @Column(name = "gas_resistance_ohms")
    private Double gasResistanceOhms;

    @Column(name = "noise_db")
    private Double noiseDb;

    // Metadata
    @Column(name = "uptime", length = 50)
    private String uptime;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
