package io.jeecloud.aidigitalsignage.digitalsignage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Digital Signage Service Application
 * 
 * Edge AI-powered digital signage system for audience analytics and reporting.
 * This service provides a single REST API endpoint for dashboard data visualization.
 * 
 * Purpose: Academic evaluation and SME demonstration
 * Data Source: Mock data only (no database persistence)
 */
@SpringBootApplication
public class DigitalSignageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalSignageServiceApplication.class, args);
    }
}
