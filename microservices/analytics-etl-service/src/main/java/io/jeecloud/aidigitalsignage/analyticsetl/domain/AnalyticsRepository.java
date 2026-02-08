package io.jeecloud.aidigitalsignage.analyticsetl.domain;

import java.util.List;

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
     * Save aggregated dashboard analytics
     * 
     * @param analytics Dashboard analytics to save
     */
    void saveDashboardAnalytics(DashboardAnalytics analytics);
    
    /**
     * Save advertisement analytics
     * 
     * @param adAnalyticsList List of ad analytics
     */
    void saveAdAnalytics(List<AdAnalytics> adAnalyticsList);
}
