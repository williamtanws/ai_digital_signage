package io.jeecloud.aidigitalsignage.digitalsignage.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Gender Distribution DTO
 */
@Getter
@Builder
public class GenderDistributionDto {
    private final Integer male;
    private final Integer female;
}
