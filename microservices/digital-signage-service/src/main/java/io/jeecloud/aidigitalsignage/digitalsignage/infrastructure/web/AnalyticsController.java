package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.web;

import io.jeecloud.aidigitalsignage.digitalsignage.application.dto.UpdateAnalyticsRequest;
import io.jeecloud.aidigitalsignage.digitalsignage.application.port.in.UpdateAnalyticsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Analytics Controller (Infrastructure Layer)
 * 
 * REST API endpoints for receiving analytics updates from ETL service.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Analytics update API (called by ETL service)")
public class AnalyticsController {
    
    private final UpdateAnalyticsUseCase updateAnalyticsUseCase;
    
    /**
     * Update analytics data
     * 
     * Called by analytics-etl-service after processing gaze events.
     * Replaces all existing analytics data.
     */
    @PostMapping("/update")
    @Operation(summary = "Update analytics data", 
               description = "Receives analytics updates from ETL service and updates SQLite database")
    public ResponseEntity<Void> updateAnalytics(@RequestBody UpdateAnalyticsRequest request) {
        log.info("POST /api/analytics/update - Received analytics update");
        
        updateAnalyticsUseCase.updateAnalytics(request);
        
        return ResponseEntity.ok().build();
    }
}
