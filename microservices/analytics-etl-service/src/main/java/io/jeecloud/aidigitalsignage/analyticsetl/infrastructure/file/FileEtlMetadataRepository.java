package io.jeecloud.aidigitalsignage.analyticsetl.infrastructure.file;

import io.jeecloud.aidigitalsignage.analyticsetl.domain.EtlMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

/**
 * File-based ETL Metadata Repository (Infrastructure Layer)
 * 
 * Stores ETL metadata in a local file to track last processed timestamp.
 * This enables incremental ETL processing (only fetch new data).
 * 
 * Uses a simple file-based storage to avoid coupling with SQLite.
 */
@Repository
@Slf4j
public class FileEtlMetadataRepository implements EtlMetadataRepository {
    
    private static final String METADATA_FILENAME = "etl-metadata.txt";
    
    @Value("${etl.metadata.directory:./data}")
    private String metadataDirectory;
    
    /**
     * Get the last processed timestamp from file
     * 
     * @return Last processed timestamp, or empty if first run
     */
    @Override
    public Optional<Instant> getLastProcessedTimestamp() {
        try {
            Path metadataPath = getMetadataPath();
            
            if (!Files.exists(metadataPath)) {
                log.debug("No metadata file found (first run)");
                return Optional.empty();
            }
            
            String content = Files.readString(metadataPath).trim();
            if (content.isEmpty()) {
                return Optional.empty();
            }
            
            // Parse format: timestamp_millis,records_processed
            String[] parts = content.split(",");
            long timestampMillis = Long.parseLong(parts[0]);
            Instant lastTimestamp = Instant.ofEpochMilli(timestampMillis);
            
            log.debug("Last processed timestamp from file: {}", lastTimestamp);
            return Optional.of(lastTimestamp);
            
        } catch (Exception e) {
            log.warn("Failed to read ETL metadata file, treating as first run", e);
            return Optional.empty();
        }
    }
    
    /**
     * Update the last processed timestamp in file
     * 
     * @param timestamp Timestamp of last processed record
     * @param recordsProcessed Number of records processed in this batch
     */
    @Override
    public void updateLastProcessedTimestamp(Instant timestamp, int recordsProcessed) {
        try {
            Path metadataPath = getMetadataPath();
            
            // Ensure directory exists
            Files.createDirectories(metadataPath.getParent());
            
            // Write format: timestamp_millis,records_processed,updated_at
            String content = String.format("%d,%d,%d",
                    timestamp.toEpochMilli(),
                    recordsProcessed,
                    Instant.now().toEpochMilli());
            
            Files.writeString(metadataPath, content);
            
            log.debug("Updated ETL metadata: timestamp={}, records={}", timestamp, recordsProcessed);
            
        } catch (IOException e) {
            log.error("Failed to write ETL metadata file", e);
            throw new RuntimeException("Failed to update ETL metadata", e);
        }
    }
    
    /**
     * Get the full path to the metadata file
     */
    private Path getMetadataPath() {
        return Paths.get(metadataDirectory, METADATA_FILENAME);
    }
}
