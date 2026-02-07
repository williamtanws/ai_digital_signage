package io.jeecloud.aidigitalsignage.digitalsignage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Advertisement Attention DTO
 * 
 * Represents attention metrics for a single advertisement.
 * Tracks whether viewers looked at the ad or not.
 * 
 * Metrics:
 * - lookYes: Number of viewers who looked at the ad
 * - lookNo: Number of viewers who did not look at the ad
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdsAttentionDto {

    /**
     * Name/identifier of the advertisement
     */
    private String adName;
    
    /**
     * Number of viewers who looked at the advertisement
     */
    private Integer lookYes;
    
    /**
     * Number of viewers who did not look at the advertisement
     */
    private Integer lookNo;
}
