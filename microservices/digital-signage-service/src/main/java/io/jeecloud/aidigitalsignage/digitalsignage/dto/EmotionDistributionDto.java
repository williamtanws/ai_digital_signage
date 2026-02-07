package io.jeecloud.aidigitalsignage.digitalsignage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Emotion Distribution DTO
 * 
 * Represents the breakdown of detected audience emotions.
 * Emotion categories based on facial expression recognition:
 * - Neutral: No significant expression
 * - Serious: Focused, concentrated expression
 * - Happy: Positive, smiling expression
 * - Surprised: Surprised, amazed expression
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionDistributionDto {

    /**
     * Number of neutral expressions detected
     */
    private Integer neutral;
    
    /**
     * Number of serious expressions detected
     */
    private Integer serious;
    
    /**
     * Number of happy expressions detected
     */
    private Integer happy;
    
    /**
     * Number of surprised expressions detected
     */
    private Integer surprised;
}
