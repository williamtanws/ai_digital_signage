package io.jeecloud.aidigitalsignage.digitalsignage.domain;

import java.util.Optional;

/**
 * Repository interface for ResearchMetrics (Domain Layer)
 * 
 * This is a port (interface) defined in the domain.
 * Implementation (adapter) will be in infrastructure layer.
 */
public interface ResearchMetricsRepository {
    
    /**
     * Find current research metrics
     */
    Optional<ResearchMetrics> findCurrent();
    
    /**
     * Save research metrics
     */
    ResearchMetrics save(ResearchMetrics researchMetrics);
    
    /**
     * Save or update baseline comparison data
     */
    void saveBaseline(Double avgEngagement, String period);
}
