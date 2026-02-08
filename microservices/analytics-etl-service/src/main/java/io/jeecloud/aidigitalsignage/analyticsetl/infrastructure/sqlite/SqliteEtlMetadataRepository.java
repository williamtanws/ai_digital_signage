package io.jeecloud.aidigitalsignage.analyticsetl.infrastructure.sqlite;

import io.jeecloud.aidigitalsignage.analyticsetl.domain.EtlMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Optional;

/**
 * SQLite ETL Metadata Repository Adapter (Infrastructure Layer)
 * 
 * Stores and retrieves ETL metadata in SQLite to track last processed timestamp.
 * This enables incremental ETL processing (only fetch new data).
 */
@Repository
@Slf4j
public class SqliteEtlMetadataRepository implements EtlMetadataRepository {
    
    private static final String METADATA_KEY = "last_processed_timestamp";
    private final JdbcTemplate jdbcTemplate;
    
    public SqliteEtlMetadataRepository(@Qualifier("sqliteDataSource") DataSource sqliteDataSource) {
        this.jdbcTemplate = new JdbcTemplate(sqliteDataSource);
        initializeTable();
    }
    
    /**
     * Create etl_metadata table if not exists
     */
    private void initializeTable() {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS etl_metadata (
                    metadata_key TEXT PRIMARY KEY,
                    last_processed_timestamp INTEGER NOT NULL,
                    records_processed INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
                """;
        
        try {
            jdbcTemplate.execute(createTableSql);
            log.debug("ETL metadata table initialized");
        } catch (Exception e) {
            log.error("Failed to create etl_metadata table", e);
            throw new RuntimeException("Failed to initialize ETL metadata table", e);
        }
    }
    
    /**
     * Get the last processed timestamp
     * 
     * @return Last processed timestamp, or empty if first run
     */
    @Override
    public Optional<Instant> getLastProcessedTimestamp() {
        String sql = """
                SELECT last_processed_timestamp
                FROM etl_metadata
                WHERE metadata_key = ?
                """;
        
        try {
            Long timestampMillis = jdbcTemplate.queryForObject(sql, Long.class, METADATA_KEY);
            if (timestampMillis != null) {
                Instant lastTimestamp = Instant.ofEpochMilli(timestampMillis);
                log.debug("Last processed timestamp: {}", lastTimestamp);
                return Optional.of(lastTimestamp);
            }
        } catch (Exception e) {
            log.debug("No last processed timestamp found (first run)");
        }
        
        return Optional.empty();
    }
    
    /**
     * Update the last processed timestamp
     * 
     * @param timestamp New timestamp to store
     * @param recordsProcessed Number of records processed
     */
    @Override
    public void updateLastProcessedTimestamp(Instant timestamp, int recordsProcessed) {
        String upsertSql = """
                INSERT INTO etl_metadata (metadata_key, last_processed_timestamp, records_processed, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(metadata_key) DO UPDATE SET
                    last_processed_timestamp = excluded.last_processed_timestamp,
                    records_processed = excluded.records_processed,
                    updated_at = excluded.updated_at
                """;
        
        try {
            int rows = jdbcTemplate.update(upsertSql,
                    METADATA_KEY,
                    timestamp.toEpochMilli(),
                    recordsProcessed,
                    Instant.now().toEpochMilli());
            
            log.info("Updated last processed timestamp to {} ({} records)", timestamp, recordsProcessed);
        } catch (Exception e) {
            log.error("Failed to update ETL metadata", e);
            throw new RuntimeException("Failed to update ETL metadata", e);
        }
    }
}
