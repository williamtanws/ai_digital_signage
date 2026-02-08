package io.jeecloud.aidigitalsignage.digitalsignage.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Age Distribution DTO
 */
@Getter
@Builder
public class AgeDistributionDto {
    private final Integer children;
    private final Integer teenagers;
    private final Integer youngAdults;
    private final Integer midAged;
    private final Integer seniors;
}
