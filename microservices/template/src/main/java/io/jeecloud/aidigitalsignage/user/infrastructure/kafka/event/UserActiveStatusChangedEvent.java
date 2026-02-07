package io.jeecloud.aidigitalsignage.user.infrastructure.kafka.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when User active status changes.
 * Part of User component - package by component principle.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActiveStatusChangedEvent {
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("active")
    private boolean active;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("eventType")
    private String eventType = "USER_ACTIVE_STATUS_CHANGED";
}
