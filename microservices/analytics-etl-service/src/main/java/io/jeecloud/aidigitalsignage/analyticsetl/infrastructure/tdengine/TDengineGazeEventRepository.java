package io.jeecloud.aidigitalsignage.analyticsetl.infrastructure.tdengine;

import io.jeecloud.aidigitalsignage.analyticsetl.domain.GazeEvent;
import io.jeecloud.aidigitalsignage.analyticsetl.domain.GazeEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/**
 * TDengine Repository Adapter (Infrastructure Layer)
 * 
 * Implements the GazeEventRepository port to extract data from TDengine.
 * TDengine is a time-series database optimized for high-volume data ingestion.
 */
@Repository
@Slf4j
public class TDengineGazeEventRepository implements GazeEventRepository {
    
    private final DataSource tdengineDataSource;
    
    public TDengineGazeEventRepository(@Qualifier("tdengineDataSource") DataSource tdengineDataSource) {
        this.tdengineDataSource = tdengineDataSource;
    }
    
    /**
     * Extract gaze events within time range
     * 
     * Query TDengine for gaze events in the specified time window.
     * Events are stored as JSON strings in event_data column.
     */
    @Override
    public List<GazeEvent> findGazeEventsBetween(Instant startTime, Instant endTime) {
        log.debug("Querying TDengine for events between {} and {}", startTime, endTime);
        
        String sql = """
                SELECT ts, event_data
                FROM gaze_events
                WHERE ts >= ? AND ts < ?
                ORDER BY ts DESC
                """;
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tdengineDataSource);
        return jdbcTemplate.query(sql, new JsonGazeEventRowMapper(),
                startTime.toEpochMilli(), endTime.toEpochMilli());
    }
    
    /**
     * Extract all session_end events
     * 
     * session_end events contain complete viewer analytics data.
     */
    @Override
    public List<GazeEvent> findAllSessionEndEvents() {
        log.debug("Querying TDengine for all session_end events");
        
        String sql = """
                SELECT ts, event_data
                FROM gaze_events
                WHERE evt_type = 'session_end'
                ORDER BY ts DESC
                """;
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tdengineDataSource);
        List<GazeEvent> events = jdbcTemplate.query(sql, new JsonGazeEventRowMapper());
        
        log.debug("Found {} session_end events", events.size());
        return events;
    }
    
    /**
     * Extract session_end events after a specific timestamp (incremental)
     * 
     * Only fetches new events for incremental ETL processing.
     * Events are stored as JSON strings in TDengine.
     */
    @Override
    public List<GazeEvent> findSessionEndEventsAfter(Instant afterTimestamp) {
        log.debug("Querying TDengine for session_end events after {}", afterTimestamp);
        
        String sql = """
                SELECT ts, event_data
                FROM gaze_events
                WHERE evt_type = 'session_end'
                AND ts > ?
                ORDER BY ts ASC
                """;
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tdengineDataSource);
        List<GazeEvent> events = jdbcTemplate.query(sql, new JsonGazeEventRowMapper(),
                afterTimestamp.toEpochMilli());
        
        log.info("Found {} NEW session_end events after {}", events.size(), afterTimestamp);
        return events;
    }
    
    /**
     * Extract all heartbeat events
     * 
     * heartbeat events contain system performance and environment metrics.
     */
    @Override
    public List<GazeEvent> findAllHeartbeatEvents() {
        log.debug("Querying TDengine for all heartbeat events");
        
        String sql = """
                SELECT ts, event_data
                FROM gaze_events
                WHERE evt_type = 'heartbeat'
                ORDER BY ts DESC
                """;
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tdengineDataSource);
        List<GazeEvent> events = jdbcTemplate.query(sql, new JsonGazeEventRowMapper());
        
        log.debug("Found {} heartbeat events", events.size());
        return events;
    }
    
    /**
     * Extract heartbeat events after a specific timestamp (incremental)
     * 
     * Only fetches new events for incremental ETL processing.
     */
    @Override
    public List<GazeEvent> findHeartbeatEventsAfter(Instant afterTimestamp) {
        log.debug("Querying TDengine for heartbeat events after {}", afterTimestamp);
        
        String sql = """
                SELECT ts, event_data
                FROM gaze_events
                WHERE evt_type = 'heartbeat'
                AND ts > ?
                ORDER BY ts ASC
                """;
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tdengineDataSource);
        List<GazeEvent> events = jdbcTemplate.query(sql, new JsonGazeEventRowMapper(),
                afterTimestamp.toEpochMilli());
        
        log.info("Found {} NEW heartbeat events after {}", events.size(), afterTimestamp);
        return events;
    }
    
    /**
     * Count unique viewers
     * 
     * Use TDengine's COUNT(DISTINCT) to get unique viewer count.
     */
    @Override
    public int countUniqueViewers() {
        log.debug("Counting unique viewers in TDengine");
        
        String sql = """
                SELECT COUNT(DISTINCT viewer_id) as unique_viewers
                FROM gaze_events
                WHERE evt_type = 'session_end'
                """;
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tdengineDataSource);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        log.debug("Unique viewers: {}", count);
        return count != null ? count : 0;
    }
    
    /**
     * RowMapper for JSON-based TDengine events
     * 
     * Parses JSON from event_data column using TDengineJsonParser.
     * Determines event type from evt_type column or JSON content.
     */
    private static class JsonGazeEventRowMapper implements RowMapper<GazeEvent> {
        @Override
        public GazeEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            long timestamp = rs.getLong("ts");
            String jsonData = rs.getString("event_data");
            
            // Try to determine event type from JSON
            String eventType = "session_end"; // default
            try {
                if (jsonData.contains("\"event\":") && jsonData.contains("heartbeat")) {
                    eventType = "heartbeat";
                }
            } catch (Exception e) {
                // Ignore, use default
            }
            
            if ("heartbeat".equals(eventType)) {
                return TDengineJsonParser.parseHeartbeatEvent(timestamp, jsonData);
            } else {
                return TDengineJsonParser.parseSessionEndEvent(timestamp, jsonData);
            }
        }
    }
}
