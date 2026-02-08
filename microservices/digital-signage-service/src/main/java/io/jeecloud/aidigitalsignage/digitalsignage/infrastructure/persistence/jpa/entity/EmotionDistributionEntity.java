package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Emotion Distribution JPA Entity (Infrastructure Layer)
 */
@Entity
@Table(name = "emotion_distribution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionDistributionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "neutral", nullable = false)
    private Integer neutral;

    @Column(name = "serious", nullable = false)
    private Integer serious;

    @Column(name = "happy", nullable = false)
    private Integer happy;

    @Column(name = "surprised", nullable = false)
    private Integer surprised;
}
