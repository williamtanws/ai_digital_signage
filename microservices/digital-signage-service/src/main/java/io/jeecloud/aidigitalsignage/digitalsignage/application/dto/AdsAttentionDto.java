package io.jeecloud.aidigitalsignage.digitalsignage.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Advertisement Attention DTO
 */
@Getter
@Builder
public class AdsAttentionDto {
    private final String adName;
    private final Integer lookYes;
    private final Integer lookNo;
}
