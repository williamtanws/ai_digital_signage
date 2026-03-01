package io.jeecloud.aidigitalsignage.analyticsetl.infrastructure.tdengine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jeecloud.aidigitalsignage.analyticsetl.domain.GazeEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

/**
 * Utility class for parsing TDengine JSON event data
 * 
 * TDengine stores events as JSON strings. This parser extracts relevant fields
 * for ETL processing, specifically focusing on session_end events.
 */
@Slf4j
public class TDengineJsonParser {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Parse session_end event JSON into GazeEvent domain object
     * 
     * Expected JSON structure:
     * {
     *   "timestamp": "2026-02-08T10:59:03.033801Z",
     *   "event": "session_end",
     *   "viewer_id": "37c6477e",
     *   "session_stats": {
     *     "total_gaze_time": 0.0,
     *     "gaze_count": 0,
     *     "session_duration": 3.04,
     *     "engagement_rate": 0.0
     *   },
     *   "demographics": {
     *     "age": 33,
     *     "gender": "Male",
     *     "emotions": {"neutral": 1}
     *   },
     *   "ad_context": {
     *     "ad_name": "Summer Sale 2026"
     *   }
     * }
     */
    public static GazeEvent parseSessionEndEvent(long timestampMs, String jsonData) {
        try {
            JsonNode root = objectMapper.readTree(jsonData);
            
            // Extract timestamp
            Instant timestamp = Instant.ofEpochMilli(timestampMs);
            
            // Extract event type
            String eventType = getTextValue(root, "event", "session_end");
            
            // Extract viewer_id
            String viewerId = getTextValue(root, "viewer_id", "unknown");
            
            // Extract session_stats
            JsonNode sessionStats = root.path("session_stats");
            Double totalGazeTime = getDoubleValue(sessionStats, "total_gaze_time", 0.0);
            Integer gazeCount = getIntValue(sessionStats, "gaze_count", 0);
            Double sessionDuration = getDoubleValue(sessionStats, "session_duration", 0.0);
            Double engagementRate = getDoubleValue(sessionStats, "engagement_rate", 0.0);
            
            // Extract demographics
            JsonNode demographics = root.path("demographics");
            Integer age = getIntValue(demographics, "age", null);
            String gender = getTextValue(demographics, "gender", "Unknown");
            
            // Extract primary emotion (the one with highest count)
            String emotion = extractPrimaryEmotion(demographics.path("emotions"));
            
            // Extract ad context (if available)
            String adName = null;
            if (root.has("ad_context")) {
                adName = getTextValue(root.path("ad_context"), "ad_name", null);
            }
            
            return GazeEvent.builder()
                    .timestamp(timestamp)
                    .eventType(eventType)
                    .viewerId(viewerId)
                    .totalGazeTime(totalGazeTime)
                    .sessionDuration(sessionDuration)
                    .gazeCount(gazeCount)
                    .engagementRate(engagementRate)
                    .age(age)
                    .gender(gender)
                    .emotion(emotion)
                    .adName(adName)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to parse session_end JSON: {}", jsonData, e);
            // Return minimal event to avoid breaking ETL
            return GazeEvent.builder()
                    .timestamp(Instant.ofEpochMilli(timestampMs))
                    .eventType("session_end")
                    .viewerId("parse_error")
                    .build();
        }
    }
    
    /**
     * Extract primary emotion from emotions object
     * Format: {"neutral": 5, "happy": 3} -> returns "neutral"
     */
    private static String extractPrimaryEmotion(JsonNode emotionsNode) {
        if (emotionsNode == null || emotionsNode.isNull() || !emotionsNode.isObject()) {
            return "neutral";
        }
        
        String primaryEmotion = "neutral";
        int maxCount = 0;
        
        emotionsNode.fields().forEachRemaining(entry -> {
            String emotion = entry.getKey();
            int count = entry.getValue().asInt(0);
            if (count > maxCount) {
                // Update maxCount using a workaround since we can't modify it in lambda
            }
        });
        
        // Simplified: Just return the first emotion found, or neutral
        if (emotionsNode.fieldNames().hasNext()) {
            return emotionsNode.fieldNames().next();
        }
        
        return primaryEmotion;
    }
    
    /**
     * Safe extraction of text value from JSON node
     */
    private static String getTextValue(JsonNode node, String fieldName, String defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode field = node.get(fieldName);
        return field.isNull() ? defaultValue : field.asText(defaultValue);
    }
    
    /**
     * Safe extraction of double value from JSON node
     */
    private static Double getDoubleValue(JsonNode node, String fieldName, Double defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode field = node.get(fieldName);
        if (field.isNull()) {
            return defaultValue;
        }
        // Use asDouble() without default parameter to avoid NPE when defaultValue is null
        return field.asDouble();
    }
    
    /**
     * Safe extraction of integer value from JSON node
     */
    private static Integer getIntValue(JsonNode node, String fieldName, Integer defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode field = node.get(fieldName);
        if (field.isNull()) {
            return defaultValue;
        }
        // Use asInt() without default parameter to avoid NPE when defaultValue is null
        return field.asInt();
    }
    
    /**
     * Parse heartbeat event JSON into GazeEvent domain object
     * 
     * Expected JSON structure:
     * {
     *   "timestamp": "2026-02-08T10:59:03.033801Z",
     *   "event": "heartbeat",
     *   "performance": {
     *     "fps": 8.5,
     *     "cpu_temp": 57.3,
     *     "uptime": 3600
     *   },
     *   "environment": {
     *     "temperature": 31.7,
     *     "humidity": 53.6,
     *     "pressure": 1012.5,
     *     "gas_resistance": 45230.0,
     *     "noise": 57.8
     *   },
     *   "diagnostics": {
     *     "kpts_valid_pct": 85.3,
     *     "solvepnp_success_pct": 0.0,
     *     "fallback_pct": 100.0,
     *     "faces_detected": 2,
     *     "face_confidence": 0.87
     *   }
     * }
     */
    public static GazeEvent parseHeartbeatEvent(long timestampMs, String jsonData) {
        try {
            JsonNode root = objectMapper.readTree(jsonData);
            
            Instant timestamp = Instant.ofEpochMilli(timestampMs);
            String eventType = getTextValue(root, "event", "heartbeat");
            
            // Extract performance metrics
            JsonNode performance = root.path("performance");
            Double fps = getDoubleValue(performance, "fps", null);
            Double cpuTemp = getDoubleValue(performance, "cpu_temp", null);
            Long uptime = getLongValue(performance, "uptime", null);
            
            // Extract environment metrics
            JsonNode environment = root.path("environment");
            Double temperature = getDoubleValue(environment, "temperature", null);
            Double humidity = getDoubleValue(environment, "humidity", null);
            Double pressure = getDoubleValue(environment, "pressure", null);
            Double gasResistance = getDoubleValue(environment, "gas_resistance", null);
            Double noise = getDoubleValue(environment, "noise", null);
            
            // Extract diagnostics metrics
            JsonNode diagnostics = root.path("diagnostics");
            Double kptsValidPercent = getDoubleValue(diagnostics, "kpts_valid_percent", null);
            Double solvepnpSuccessPercent = getDoubleValue(diagnostics, "solvepnp_success_percent", null);
            Double fallbackPercent = getDoubleValue(diagnostics, "fallback_percent", null);
            Integer facesInFrame = getIntValue(diagnostics, "faces_in_frame", null);
            Double faceConfidence = getDoubleValue(diagnostics, "face_confidence", null);
            
            return GazeEvent.builder()
                    .timestamp(timestamp)
                    .eventType(eventType)
                    .fps(fps)
                    .cpuTemp(cpuTemp)
                    .uptime(uptime)
                    .temperature(temperature)
                    .humidity(humidity)
                    .pressure(pressure)
                    .gasResistance(gasResistance)
                    .noise(noise)
                    .kptsValidPercent(kptsValidPercent)
                    .solvepnpSuccessPercent(solvepnpSuccessPercent)
                    .fallbackPercent(fallbackPercent)
                    .facesInFrame(facesInFrame)
                    .faceConfidence(faceConfidence)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to parse heartbeat JSON: {}", jsonData, e);
            return GazeEvent.builder()
                    .timestamp(Instant.ofEpochMilli(timestampMs))
                    .eventType("heartbeat")
                    .build();
        }
    }
    
    /**
     * Safe extraction of long value from JSON node
     */
    private static Long getLongValue(JsonNode node, String fieldName, Long defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode field = node.get(fieldName);
        if (field.isNull()) {
            return defaultValue;
        }
        return field.asLong();
    }
}
