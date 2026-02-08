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
     */
    @Override
    public List<GazeEvent> findGazeEventsBetween(Instant startTime, Instant endTime) {
        log.debug("Querying TDengine for events between {} and {}", startTime, endTime);
        
        String sql = """
                SELECT ts, evt_type as event_type, viewer_id, gaze_time as total_gaze_time, session_duration,
                       interested as gaze_count, attention_rate as engagement_rate, age, gender, emotion, ad_name
                FROM gaze_events
                WHERE ts >= ? AND ts < ?
                ORDER BY ts DESC
                """;
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tdengineDataSource);
        return jdbcTemplate.query(sql, new GazeEventRowMapper(),
                startTime.toEpochMilli(), endTime.toEpochMilli());
    }
    
    /**
     * Extract all session_end events
     * 
     * session_end events contain complete viewer analytics data
     * (demographics, session duration, engagement rate).
     */
    @Override
    public List<GazeEvent> findAllSessionEndEvents() {
        log.debug("Querying TDengine for all session_end events");
        
        String sql = """
                SELECT ts, evt_type, viewer_id, gaze_time, session_duration,
                       interested, attention_rate, age, gender, emotion, ad_name
                FROM gaze_events
                WHERE evt_type = 'session_end'
                ORDER BY ts DESC
                """;
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tdengineDataSource);
        List<GazeEvent> events = jdbcTemplate.query(sql, new TDengineGazeEventRowMapper());
        
        log.debug("Found {} session_end events", events.size());
        return events;
    }
    
    /**
     * Extract session_end events after a specific timestamp (incremental)
     * 
     * Only fetches new events for incremental ETL processing.
     */
    @Override
    public List<GazeEvent> findSessionEndEventsAfter(Instant afterTimestamp) {
        log.debug("Querying TDengine for session_end events after {}", afterTimestamp);
        
        String sql = """
                SELECT ts, evt_type, viewer_id, gaze_time, session_duration,
                       interested, attention_rate, age, gender, emotion, ad_name
                FROM gaze_events
                WHERE evt_type = 'session_end'
                AND ts > ?
                ORDER BY ts ASC
                """;
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tdengineDataSource);
        List<GazeEvent> events = jdbcTemplate.query(sql, new TDengineGazeEventRowMapper(),
                afterTimestamp.toEpochMilli());
        
        log.info("Found {} NEW session_end events after {}", events.size(), afterTimestamp);
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
     * RowMapper for TDengine result set - maps TDengine column names to domain object
     */
    private static class TDengineGazeEventRowMapper implements RowMapper<GazeEvent> {
        @Override
        public GazeEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return GazeEvent.builder()
                    .timestamp(Instant.ofEpochMilli(rs.getLong("ts")))
                    .eventType(rs.getString("evt_type"))  // TDengine column: evt_type
                    .viewerId(rs.getString("viewer_id"))
                    .totalGazeTime(getDoubleOrNull(rs, "gaze_time"))  // TDengine column: gaze_time
                    .sessionDuration(getDoubleOrNull(rs, "session_duration"))
                    .gazeCount(getIntOrNull(rs, "interested"))  // TDengine column: interested
                    .engagementRate(getDoubleOrNull(rs, "attention_rate"))  // TDengine column: attention_rate
                    .age(getIntOrNull(rs, "age"))
                    .gender(rs.getString("gender"))
                    .emotion(rs.getString("emotion"))
                    .adName(rs.getString("ad_name"))
                    .build();
        }
        
        private Double getDoubleOrNull(ResultSet rs, String columnName) throws SQLException {
            double value = rs.getDouble(columnName);
            return rs.wasNull() ? null : value;
        }
        
        private Integer getIntOrNull(ResultSet rs, String columnName) throws SQLException {
            int value = rs.getInt(columnName);
            return rs.wasNull() ? null : value;
        }
    }
    
    /**
     * RowMapper to convert TDengine ResultSet to GazeEvent domain object
     */
    private static class GazeEventRowMapper implements RowMapper<GazeEvent> {
        @Override
        public GazeEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return GazeEvent.builder()
                    .timestamp(Instant.ofEpochMilli(rs.getLong("ts")))
                    .eventType(rs.getString("event_type"))
                    .viewerId(rs.getString("viewer_id"))
                    .totalGazeTime(getDoubleOrNull(rs, "total_gaze_time"))
                    .sessionDuration(getDoubleOrNull(rs, "session_duration"))
                    .gazeCount(getIntOrNull(rs, "gaze_count"))
                    .engagementRate(getDoubleOrNull(rs, "engagement_rate"))
                    .age(getIntOrNull(rs, "age"))
                    .gender(rs.getString("gender"))
                    .emotion(rs.getString("emotion"))
                    .adName(rs.getString("ad_name"))
                    .build();
        }
        
        private Double getDoubleOrNull(ResultSet rs, String columnName) throws SQLException {
            double value = rs.getDouble(columnName);
            return rs.wasNull() ? null : value;
        }
        
        private Integer getIntOrNull(ResultSet rs, String columnName) throws SQLException {
            int value = rs.getInt(columnName);
            return rs.wasNull() ? null : value;
        }
    }
}
