package io.jeecloud.aidigitalsignage.analyticsetl.infrastructure.rest;

import io.jeecloud.aidigitalsignage.analyticsetl.application.AnalyticsEtlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * ETL Controller (Infrastructure Layer)
 * 
 * REST endpoint to manually trigger the ETL process.
 * Useful for testing, development, and on-demand data synchronization.
 */
@RestController
@RequestMapping("/api/etl")
@RequiredArgsConstructor
@Slf4j
public class EtlController {
    
    private final AnalyticsEtlService etlService;
    
    /**
     * Trigger ETL execution manually
     * 
     * POST /api/etl/trigger
     * 
     * @return JSON response with status and timestamp
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerEtl() {
        log.info(">>> Manual ETL trigger received via REST API");
        
        try {
            long startTime = System.currentTimeMillis();
            etlService.executeEtl();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info(">>> Manual ETL completed in {}ms", duration);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "ETL process completed successfully",
                "timestamp", Instant.now().toString(),
                "durationMs", duration
            ));
            
        } catch (Exception e) {
            log.error(">>> Manual ETL failed: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
            ));
        }
    }
}
