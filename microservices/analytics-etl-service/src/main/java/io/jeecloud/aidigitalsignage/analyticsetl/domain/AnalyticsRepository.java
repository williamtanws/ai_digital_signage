package io.jeecloud.aidigitalsignage.analyticsetl.domain;

import io.jeecloud.aidigitalsignage.analyticsetl.application.dto.ResearchMetricsDto;
import io.jeecloud.aidigitalsignage.analyticsetl.application.dto.SystemHealthDto;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface: AnalyticsRepository (Port)
 * 
 * Domain interface for loading analytics into SQLite.
 * Implementation will be in infrastructure layer.
 */
public interface AnalyticsRepository {
    
    /**
     * Clear existing analytics data (for fresh ETL run)
     */
    void clearAllAnalytics();
    
    /**
     * Get existing dashboard analytics for accumulation mode
     * 
     * @return Optional containing existing analytics if available
     */
    Optional<DashboardAnalytics> getExistingDashboardAnalytics();
    
    /**
     * Get existing ad analytics for accumulation mode
     * 
     * @return List of existing ad analytics
     */
    List<AdAnalytics> getExistingAdAnalytics();
    
    /**
     * Save aggregated dashboard analytics
     * 
     * @param analytics Dashboard analytics to save
     */
    void saveDashboardAnalytics(DashboardAnalytics analytics);
    
    /**
     * Save advertisement analytics (also sends dashboard analytics)
     * 
     * @param adAnalyticsList List of ad analytics
     * @param systemHealth System health metrics (nullable)
     * @param researchMetrics Research validation metrics (nullable)
     */
    void saveAdAnalytics(List<AdAnalytics> adAnalyticsList, SystemHealthDto systemHealth, ResearchMetricsDto researchMetrics);
}
