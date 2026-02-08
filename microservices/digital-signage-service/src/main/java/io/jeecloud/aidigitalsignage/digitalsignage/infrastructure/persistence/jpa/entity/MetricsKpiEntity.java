package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Metrics KPI JPA Entity (Infrastructure Layer)
 * 
 * Persistence model separate from domain model.
 * Contains JPA annotations for database mapping.
 */
@Entity
@Table(name = "metrics_kpi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricsKpiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_audience", nullable = false)
    private Integer totalAudience;

    @Column(name = "total_views", nullable = false)
    private Integer totalViews;

    @Column(name = "total_ads", nullable = false)
    private Integer totalAds;

    @Column(name = "avg_view_seconds", nullable = false)
    private Double avgViewSeconds;
}
