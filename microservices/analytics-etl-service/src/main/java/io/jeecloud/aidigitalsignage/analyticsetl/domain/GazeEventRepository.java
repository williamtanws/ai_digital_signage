package io.jeecloud.aidigitalsignage.analyticsetl.domain;

import java.time.Instant;
import java.util.List;

/**
 * Repository Interface: GazeEventRepository (Port)
 * 
 * Domain interface for extracting gaze events from TDengine.
 * Implementation will be in infrastructure layer.
 */
public interface GazeEventRepository {
    
    /**
     * Extract gaze events within a time range
     * 
     * @param startTime Start of time range
     * @param endTime End of time range
     * @return List of gaze events
     */
    List<GazeEvent> findGazeEventsBetween(Instant startTime, Instant endTime);
    
    /**
     * Extract all session_end events (contains complete viewer data)
     * 
     * @return List of session end events
     */
    List<GazeEvent> findAllSessionEndEvents();
    
    /**
     * Extract session_end events after a specific timestamp (incremental)
     * 
     * @param afterTimestamp Only return events after this timestamp
     * @return List of session end events
     */
    List<GazeEvent> findSessionEndEventsAfter(Instant afterTimestamp);
    
    /**
     * Count total unique viewers
     * 
     * @return Total unique viewers
     */
    int countUniqueViewers();
    
    /**
     * Extract all heartbeat events (contains system performance data)
     * 
     * @return List of heartbeat events
     */
    List<GazeEvent> findAllHeartbeatEvents();
    
    /**
     * Extract heartbeat events after a specific timestamp (incremental)
     * 
     * @param afterTimestamp Only return events after this timestamp
     * @return List of heartbeat events
     */
    List<GazeEvent> findHeartbeatEventsAfter(Instant afterTimestamp);
}
