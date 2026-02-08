package io.jeecloud.aidigitalsignage.analyticsetl.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Domain Entity: AdAnalytics
 * 
 * Represents per-advertisement analytics.
 */
@Data
@Builder
public class AdAnalytics {
    
    /**
     * Advertisement name
     */
    private String adName;
    
    /**
     * Total viewers who saw this ad
     */
    private Integer totalViewers;
    
    /**
     * Viewers who looked at the ad (engaged)
     */
    private Integer lookYes;
    
    /**
     * Viewers who didn't look at the ad
     */
    private Integer lookNo;
    
    /**
     * Calculate attention rate
     */
    public double getAttentionRate() {
        if (totalViewers == 0) return 0.0;
        return (double) lookYes / totalViewers;
    }
}
