package io.jeecloud.aidigitalsignage.digitalsignage.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Advertisement Performance DTO
 */
@Getter
@Builder
public class AdsPerformanceDto {
    private final String adName;
    private final Integer totalViewers;
}
