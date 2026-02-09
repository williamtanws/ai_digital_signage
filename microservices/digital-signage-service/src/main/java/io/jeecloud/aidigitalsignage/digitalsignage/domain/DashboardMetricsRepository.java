package io.jeecloud.aidigitalsignage.digitalsignage.domain;

import java.util.List;
import java.util.Optional;

/**
 * Dashboard Metrics Repository Port (Domain Interface)
 * 
 * Defines contract for accessing dashboard metrics.
 * Implementation in infrastructure layer.
 */
public interface DashboardMetricsRepository {

    /**
     * Retrieve current dashboard metrics
     */
    Optional<DashboardMetrics> findCurrent();
    
    /**
     * Save dashboard metrics
     */
    DashboardMetrics save(DashboardMetrics metrics);
    
    /**
     * Delete all dashboard metrics
     */
    void deleteAll();
}
