package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Advertisement JPA Entity (Infrastructure Layer)
 */
@Entity
@Table(name = "advertisement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertisementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ad_name", nullable = false, unique = true, length = 100)
    private String adName;

    @Column(name = "total_viewers", nullable = false)
    private Integer totalViewers;

    @Column(name = "look_yes", nullable = false)
    private Integer lookYes;

    @Column(name = "look_no", nullable = false)
    private Integer lookNo;
}
