package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Gender Distribution JPA Entity (Infrastructure Layer)
 */
@Entity
@Table(name = "gender_distribution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenderDistributionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "male", nullable = false)
    private Integer male;

    @Column(name = "female", nullable = false)
    private Integer female;
}
