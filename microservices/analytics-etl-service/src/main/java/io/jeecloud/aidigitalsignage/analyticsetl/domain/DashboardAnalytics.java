package io.jeecloud.aidigitalsignage.analyticsetl.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Domain Entity: DashboardAnalytics
 * 
 * Represents aggregated analytics to be loaded into SQLite.
 * This matches the schema from digital-signage-service.
 */
@Data
@Builder
public class DashboardAnalytics {
    
    // KPI Metrics
    private Integer totalAudience;
    private Integer totalViews;
    private Integer totalAds;
    private Double avgViewSeconds;
    
    // Age Distribution
    private Integer children;      // 0-12
    private Integer teenagers;     // 13-19
    private Integer youngAdults;   // 20-35
    private Integer midAged;       // 36-55
    private Integer seniors;       // 56+
    
    // Gender Distribution
    private Integer male;
    private Integer female;
    
    // Emotion Distribution
    private Integer neutral;
    private Integer serious;
    private Integer happy;
    private Integer surprised;
}
