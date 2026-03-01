package io.jeecloud.aidigitalsignage.digitalsignage.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Emotion Distribution DTO (FER2013 - 8 emotions)
 */
@Getter
@Builder
public class EmotionDistributionDto {
    private final Integer anger;
    private final Integer contempt;
    private final Integer disgust;
    private final Integer fear;
    private final Integer happiness;
    private final Integer neutral;
    private final Integer sadness;
    private final Integer surprise;
}
