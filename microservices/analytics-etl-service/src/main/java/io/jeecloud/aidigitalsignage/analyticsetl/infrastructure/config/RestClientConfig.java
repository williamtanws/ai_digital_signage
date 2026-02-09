package io.jeecloud.aidigitalsignage.analyticsetl.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * REST Client Configuration
 * 
 * Provides RestTemplate bean for calling digital-signage-service API.
 */
@Configuration
public class RestClientConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
