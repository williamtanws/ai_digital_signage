package io.jeecloud.aidigitalsignage.digitalsignage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Advertisement Performance DTO
 * 
 * Represents performance metrics for a single advertisement.
 * Tracks the total number of viewers who saw the ad.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdsPerformanceDto {

    /**
     * Name/identifier of the advertisement
     */
    private String adName;
    
    /**
     * Total number of viewers who saw this advertisement
     */
    private Integer totalViewers;
}
