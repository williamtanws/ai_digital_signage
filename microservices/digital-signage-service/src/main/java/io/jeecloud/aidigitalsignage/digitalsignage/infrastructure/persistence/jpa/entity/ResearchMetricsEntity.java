package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Research Metrics JPA Entity (Infrastructure Layer)
 * 
 * Persistence model for research validation metrics.
 * Contains JPA annotations for database mapping.
 */
@Entity
@Table(name = "research_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchMetricsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Face detection metrics
    @Column(name = "face_detection_accuracy")
    private Double faceDetectionAccuracy;

    @Column(name = "face_detection_confidence")
    private Double faceDetectionConfidence;

    @Column(name = "frames_processed")
    private Integer framesProcessed;

    @Column(name = "faces_detected")
    private Integer facesDetected;

    // Gaze quality metrics
    @Column(name = "primary_method_rate")
    private Double primaryMethodRate;

    @Column(name = "fallback_method_rate")
    private Double fallbackMethodRate;

    @Column(name = "gaze_avg_confidence")
    private Double gazeAvgConfidence;

    @Column(name = "gaze_quality_score", length = 20)
    private String gazeQualityScore;

    @Column(name = "gaze_recommendation")
    private String gazeRecommendation;

    // Baseline comparison (static signage)
    @Column(name = "baseline_condition", length = 50)
    private String baselineCondition;

    @Column(name = "baseline_avg_engagement")
    private Double baselineAvgEngagement;

    @Column(name = "baseline_period", length = 100)
    private String baselinePeriod;

    // Current comparison (dynamic signage)
    @Column(name = "current_condition", length = 50)
    private String currentCondition;

    @Column(name = "current_avg_engagement")
    private Double currentAvgEngagement;

    @Column(name = "current_period", length = 100)
    private String currentPeriod;

    // Improvement metrics
    @Column(name = "improvement_absolute")
    private Double improvementAbsolute;

    @Column(name = "improvement_percentage")
    private Double improvementPercentage;

    @Column(name = "improvement_significant")
    private Boolean improvementSignificant;

    // Metadata
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
