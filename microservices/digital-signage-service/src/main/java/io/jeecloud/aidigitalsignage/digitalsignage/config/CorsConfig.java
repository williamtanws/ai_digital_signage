package io.jeecloud.aidigitalsignage.digitalsignage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration
 * 
 * Configures Cross-Origin Resource Sharing to allow the frontend
 * dashboard (Vue.js) to access the backend API.
 * 
 * Development Settings:
 * - Allows localhost origins (Vue dev server typically runs on 3000 or 5173)
 * - Allows all HTTP methods
 * - Allows all headers
 * - Enables credentials
 * 
 * Note: For production deployment, restrict origins to specific domains.
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "http://localhost:3000",    // Common React/Vue port
                                "http://localhost:5173",    // Vite default port
                                "http://localhost:8081",    // Alternative Vue port
                                "http://127.0.0.1:3000",
                                "http://127.0.0.1:5173",
                                "http://127.0.0.1:8081"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
