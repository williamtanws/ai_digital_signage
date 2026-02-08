package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration (Infrastructure Layer)
 * 
 * Configures Swagger UI and OpenAPI documentation for REST API endpoints.
 * 
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access OpenAPI JSON at: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI digitalSignageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Signage Dashboard API")
                        .description("""
                                REST API for Edge AI Digital Signage Analytics Dashboard.
                                
                                This service provides real-time analytics aggregated from TDengine time-series data,
                                including viewer demographics, emotions, and advertisement performance metrics.
                                
                                **Data Flow:**
                                - TDengine (Time-series DB) → analytics-etl-service → SQLite → digital-signage-service → Dashboard
                                
                                **Features:**
                                - Real-time audience analytics
                                - Demographic breakdowns (age, gender)
                                - Emotion detection metrics
                                - Advertisement performance tracking
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("JeeCloud AI Digital Signage Team")
                                .email("support@jeecloud.io")
                                .url("https://jeecloud.io"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Production Server (Update with actual URL)")
                ));
    }
}
