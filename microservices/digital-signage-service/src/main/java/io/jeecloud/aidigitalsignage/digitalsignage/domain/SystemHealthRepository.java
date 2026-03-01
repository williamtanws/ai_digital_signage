package io.jeecloud.aidigitalsignage.digitalsignage.domain;

import java.util.Optional;

/**
 * Repository interface for SystemHealth (Domain Layer)
 * 
 * This is a port (interface) defined in the domain.
 * Implementation (adapter) will be in infrastructure layer.
 */
public interface SystemHealthRepository {
    
    /**
     * Find current system health metrics
     */
    Optional<SystemHealth> findCurrent();
    
    /**
     * Save system health metrics
     */
    SystemHealth save(SystemHealth systemHealth);
}
