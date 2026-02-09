package io.jeecloud.aidigitalsignage.analyticsetl;

import io.jeecloud.aidigitalsignage.analyticsetl.application.AnalyticsEtlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Analytics ETL Service - Main Application
 * 
 * This service extracts gaze events from TDengine, transforms them
 * into analytics, and loads them into SQLite for the dashboard.
 * 
 * ETL Pipeline:
 * 1. EXTRACT: Read GAZE_EVENT records from TDengine (incremental)
 * 2. TRANSFORM: Aggregate into dashboard and ad analytics
 * 3. LOAD: Insert into SQLite (digital-signage-service database)
 * 
 * Scheduling:
 * - Runs on startup (CommandLineRunner)
 * - Runs every 5 minutes (@Scheduled)
 * - Only fetches NEW data (timestamp-based incremental ETL)
 */
@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEtlApplication implements CommandLineRunner {
    
    private final AnalyticsEtlService etlService;
    
    public static void main(String[] args) {
        SpringApplication.run(AnalyticsEtlApplication.class, args);
    }
    
    /**
     * Execute ETL on startup
     * 
     * Runs once immediately when the service starts.
     */
    @Override
    public void run(String... args) {
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║     Analytics ETL Service - Starting Pipeline         ║");
        log.info("║     Mode: Continuous (Scheduled every 5 minutes)      ║");
        log.info("╚════════════════════════════════════════════════════════╝");
        
        try {
            etlService.executeEtl();
            
            log.info("╔════════════════════════════════════════════════════════╗");
            log.info("║     Initial ETL Pipeline - Completed Successfully     ║");
            log.info("║     Next run: In 5 minutes                            ║");
            log.info("╚════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            log.error("╔════════════════════════════════════════════════════════╗");
            log.error("║     Analytics ETL Pipeline - Failed                   ║");
            log.error("╚════════════════════════════════════════════════════════╝");
            log.error("Error: {}", e.getMessage());
            // Don't exit - let scheduled tasks continue
        }
    }
    
    /**
     * Scheduled ETL execution
     * 
     * Runs every 5 minutes to fetch and process NEW data.
     * Only extracts events after the last processed timestamp.
     */
    @Scheduled(initialDelay = 60000, fixedRate = 300000) // Wait 1 minute after startup, then run every 5 minutes
    public void scheduleEtl() {
        log.info(">>> Scheduled ETL - Starting (every 5 minutes)");
        
        try {
            etlService.executeEtl();
            log.info(">>> Scheduled ETL - Completed");
        } catch (Exception e) {
            log.error(">>> Scheduled ETL - Failed: {}", e.getMessage(), e);
            // Don't stop - next scheduled run will retry
        }
    }
}
