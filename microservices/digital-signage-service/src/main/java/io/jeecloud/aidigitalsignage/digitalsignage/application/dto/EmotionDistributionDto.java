package io.jeecloud.aidigitalsignage.digitalsignage.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Emotion Distribution DTO
 */
@Getter
@Builder
public class EmotionDistributionDto {
    private final Integer neutral;
    private final Integer serious;
    private final Integer happy;
    private final Integer surprised;
}
