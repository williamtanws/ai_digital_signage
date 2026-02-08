package io.jeecloud.aidigitalsignage.analyticsetl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain Entity: ETL Metadata
 * 
 * Tracks the last processed timestamp for incremental ETL.
 * Only new data after this timestamp will be processed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtlMetadata {
    
    /**
     * Metadata key (e.g., "last_processed_timestamp")
     */
    private String metadataKey;
    
    /**
     * Last successfully processed timestamp
     */
    private Instant lastProcessedTimestamp;
    
    /**
     * Total records processed in last run
     */
    private Integer recordsProcessed;
    
    /**
     * Last update time
     */
    private Instant updatedAt;
}
