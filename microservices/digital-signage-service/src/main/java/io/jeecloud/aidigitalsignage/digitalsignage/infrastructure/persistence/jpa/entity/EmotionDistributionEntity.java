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

    @Column(name = "anger", nullable = false)
    private Integer anger;

    @Column(name = "contempt", nullable = false)
    private Integer contempt;

    @Column(name = "disgust", nullable = false)
    private Integer disgust;

    @Column(name = "fear", nullable = false)
    private Integer fear;

    @Column(name = "happiness", nullable = false)
    private Integer happiness;

    @Column(name = "neutral", nullable = false)
    private Integer neutral;

    @Column(name = "sadness", nullable = false)
    private Integer sadness;

    @Column(name = "surprise", nullable = false)
    private Integer surprise;
}
