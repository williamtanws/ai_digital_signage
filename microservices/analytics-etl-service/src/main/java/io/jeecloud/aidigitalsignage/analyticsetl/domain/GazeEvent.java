package io.jeecloud.aidigitalsignage.analyticsetl.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Domain Entity: GazeEvent
 * 
 * Represents a raw gaze event from TDengine.
 * This is extracted from the audience-analysis-service logs.
 * 
 * Event Types:
 * - gaze_start: When viewer starts looking at screen
 * - session_end: When viewer session completes
 * - heartbeat: Periodic status update
 */
@Data
@Builder
public class GazeEvent {
    
    /**
     * Timestamp when the event occurred
     */
    private Instant timestamp;
    
    /**
     * Event type: gaze_start, session_end, heartbeat
     */
    private String eventType;
    
    /**
     * Unique viewer identifier (from face recognition)
     */
    private String viewerId;
    
    /**
     * Total gaze time in seconds (from session_end events)
     */
    private Double totalGazeTime;
    
    /**
     * Session duration in seconds (from session_end events)
     */
    private Double sessionDuration;
    
    /**
     * Number of times viewer gazed at screen
     */
    private Integer gazeCount;
    
    /**
     * Engagement rate (0.0 to 1.0)
     */
    private Double engagementRate;
    
    /**
     * Viewer age (from demographics)
     */
    private Integer age;
    
    /**
     * Viewer gender (Male/Female)
     */
    private String gender;
    
    /**
     * Viewer emotion (neutral, happy, serious, surprised, etc.)
     */
    private String emotion;
    
    /**
     * Advertisement being displayed (if available)
     */
    private String adName;
    
    // === HEARTBEAT EVENT FIELDS ===
    
    /**
     * Frames per second (from heartbeat events)
     */
    private Double fps;
    
    /**
     * CPU temperature in Celsius (from heartbeat events)
     */
    private Double cpuTemp;
    
    /**
     * System uptime in seconds (from heartbeat events)
     */
    private Long uptime;
    
    /**
     * Ambient temperature in Celsius (from BME688 sensor)
     */
    private Double temperature;
    
    /**
     * Humidity percentage (from BME688 sensor)
     */
    private Double humidity;
    
    /**
     * Atmospheric pressure in hPa (from BME688 sensor)
     */
    private Double pressure;
    
    /**
     * Gas resistance in Ohms (from BME688 sensor)
     */
    private Double gasResistance;
    
    /**
     * Noise level in dB (from microphone)
     */
    private Double noise;
    
    /**
     * Percentage of valid keypoints (gaze quality)
     */
    private Double kptsValidPercent;
    
    /**
     * SolvePnP success percentage (gaze quality)
     */
    private Double solvepnpSuccessPercent;
    
    /**
     * Fallback mode percentage (gaze quality)
     */
    private Double fallbackPercent;
    
    /**
     * Number of faces detected in frame (from face detection)
     */
    private Integer facesInFrame;
    
    /**
     * Face detection confidence (0.0 to 1.0)
     */
    private Double faceConfidence;
}
