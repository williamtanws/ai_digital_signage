package io.jeecloud.aidigitalsignage.analyticsetl.domain;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository Interface: EtlMetadataRepository (Port)
 * 
 * Domain interface for managing ETL metadata (last processed timestamp).
 * Enables incremental ETL processing.
 */
public interface EtlMetadataRepository {
    
    /**
     * Get the last processed timestamp
     * 
     * @return Last processed timestamp, or empty if first run
     */
    Optional<Instant> getLastProcessedTimestamp();
    
    /**
     * Update the last processed timestamp
     * 
     * @param timestamp New timestamp to store
     * @param recordsProcessed Number of records processed
     */
    void updateLastProcessedTimestamp(Instant timestamp, int recordsProcessed);
}
