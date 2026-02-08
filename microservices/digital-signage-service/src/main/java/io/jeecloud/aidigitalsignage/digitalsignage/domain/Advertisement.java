package io.jeecloud.aidigitalsignage.digitalsignage.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * Advertisement Aggregate Root
 * 
 * Domain entity representing an advertisement with performance metrics.
 * Pure business logic - no framework dependencies.
 */
@Getter
@Builder
public class Advertisement {

    private final Long id;
    private final String adName;
    private final Integer totalViewers;
    private final Integer lookYes;
    private final Integer lookNo;

    /**
     * Calculate attention rate percentage
     */
    public double getAttentionRate() {
        if (totalViewers == null || totalViewers == 0) {
            return 0.0;
        }
        return (lookYes.doubleValue() / totalViewers) * 100;
    }

    /**
     * Check if this ad has high engagement (>70% attention)
     */
    public boolean isHighEngagement() {
        return getAttentionRate() >= 70.0;
    }

    /**
     * Validate advertisement data
     */
    public boolean isValid() {
        return adName != null && !adName.isBlank() 
            && totalViewers != null && totalViewers >= 0
            && lookYes != null && lookYes >= 0
            && lookNo != null && lookNo >= 0
            && (lookYes + lookNo) <= totalViewers;
    }
}
