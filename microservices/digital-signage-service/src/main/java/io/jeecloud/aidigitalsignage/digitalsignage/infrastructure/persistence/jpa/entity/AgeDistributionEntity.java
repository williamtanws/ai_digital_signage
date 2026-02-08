package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Age Distribution JPA Entity (Infrastructure Layer)
 */
@Entity
@Table(name = "age_distribution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgeDistributionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "children", nullable = false)
    private Integer children;

    @Column(name = "teenagers", nullable = false)
    private Integer teenagers;

    @Column(name = "young_adults", nullable = false)
    private Integer youngAdults;

    @Column(name = "mid_aged", nullable = false)
    private Integer midAged;

    @Column(name = "seniors", nullable = false)
    private Integer seniors;
}
