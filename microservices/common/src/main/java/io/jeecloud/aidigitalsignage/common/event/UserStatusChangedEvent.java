package io.jeecloud.aidigitalsignage.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Shared event published when User status changes.
 * This is a cross-component event that can be consumed by multiple components.
 * Placed in common package to allow cross-component communication without violating
 * architectural boundaries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusChangedEvent {
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("agentCode")
    private String agentCode;
    
    @JsonProperty("status")
    private boolean status;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("eventType")
    private String eventType = "USER_STATUS_CHANGED";
}
