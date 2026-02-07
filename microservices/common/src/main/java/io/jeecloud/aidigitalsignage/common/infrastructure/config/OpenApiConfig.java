package io.jeecloud.aidigitalsignage.common.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration.
 * Provides API documentation and interactive API explorer (Swagger UI).
 * 
 * Accessible at:
 * - Swagger UI: /swagger-ui.html
 * - OpenAPI Spec: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Allianz SAT Microservices API")
                .version("1.0.0")
                .description("""
                    RESTful API for Allianz SAT (Sales & Agency Transformation) platform.
                    
                    This service follows Package by Component architecture with:
                    - Domain-Driven Design (DDD)
                    - Hexagonal Architecture
                    - CQRS Pattern
                    
                    Deployment: AWS FCP/SCC+ (Future Cloud Platform/Smart Cloud Connect+)
                    """)
                .contact(new Contact()
                    .name("Allianz SAT Team")
                    .email("sat-support@allianz.com"))
                .license(new License()
                    .name("Allianz Proprietary")
                    .url("https://www.allianz.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Development Server"),
                new Server()
                    .url("https://sat-api-uat.allianz.com")
                    .description("UAT Server"),
                new Server()
                    .url("https://sat-api.allianz.com")
                    .description("Production Server")
            ));
    }
}
