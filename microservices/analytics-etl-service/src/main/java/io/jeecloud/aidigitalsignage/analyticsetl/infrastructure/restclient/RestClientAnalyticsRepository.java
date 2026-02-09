package io.jeecloud.aidigitalsignage.analyticsetl.infrastructure.restclient;

import io.jeecloud.aidigitalsignage.analyticsetl.domain.AdAnalytics;
import io.jeecloud.aidigitalsignage.analyticsetl.domain.AnalyticsRepository;
import io.jeecloud.aidigitalsignage.analyticsetl.domain.DashboardAnalytics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Client Repository Adapter (Infrastructure Layer)
 * 
 * Implements the AnalyticsRepository port by calling digital-signage-service REST API.
 * Follows microservice best practices - each service owns its own database.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RestClientAnalyticsRepository implements AnalyticsRepository {
    
    private final RestTemplate restTemplate;
    
    @Value("${digital-signage-service.url:http://localhost:8080}")
    private String digitalSignageServiceUrl;
    
    // Temporary storage for dashboard analytics (within single ETL transaction)
    private DashboardAnalytics currentDashboardAnalytics;
    
    /**
     * Clear all analytics - not needed for REST API approach
     * The digital-signage-service will handle clearing old data
     */
    @Override
    public void clearAllAnalytics() {
        log.debug("clearAllAnalytics() - handled by digital-signage-service");
        // No-op: clearing is handled by the receiving service
    }
    
    /**
     * Store dashboard analytics temporarily for batch update
     * 
     * The analytics will be sent together with ad analytics in saveAdAnalytics()
     */
    @Override
    public void saveDashboardAnalytics(DashboardAnalytics analytics) {
        log.debug("Storing dashboard analytics for batch REST API call");
        this.currentDashboardAnalytics = analytics;
    }
    
    /**
     * Save advertisement analytics via REST API
     * 
     * This method sends both dashboard and ad analytics together.
     */
    @Override
    public void saveAdAnalytics(List<AdAnalytics> adAnalyticsList) {
        try {
            String url = digitalSignageServiceUrl + "/api/analytics/update";
            
            // Build request payload with both dashboard and ad analytics
            Map<String, Object> request = buildUpdateRequest(currentDashboardAnalytics, adAnalyticsList);
            
            log.info("Sending analytics update to digital-signage-service: {}", url);
            log.debug("Payload: dashboard metrics + {} ad analytics", adAnalyticsList.size());
            
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully sent analytics to digital-signage-service");
            } else {
                log.warn("Unexpected response from digital-signage-service: {}", response.getStatusCode());
            }
            
            // Clear temporary storage
            this.currentDashboardAnalytics = null;
            
        } catch (Exception e) {
            log.error("Failed to send analytics to digital-signage-service", e);
            throw new RuntimeException("Failed to update analytics via REST API", e);
        }
    }
    
    /**
     * Build the request payload for the REST API call
     */
    private Map<String, Object> buildUpdateRequest(DashboardAnalytics dashboard, List<AdAnalytics> adAnalyticsList) {
        Map<String, Object> request = new HashMap<>();
        
        // Map dashboard metrics
        request.put("dashboardMetrics", mapDashboardAnalytics(dashboard));
        
        // Map ad analytics
        List<Map<String, Object>> adMetrics = adAnalyticsList.stream()
                .map(this::mapAdAnalytics)
                .collect(Collectors.toList());
        request.put("adMetrics", adMetrics);
        
        return request;
    }
    
    /**
     * Map domain DashboardAnalytics to DTO map
     */
    private Map<String, Object> mapDashboardAnalytics(DashboardAnalytics analytics) {
        Map<String, Object> map = new HashMap<>();
        
        // KPI Metrics
        map.put("totalAudience", analytics.getTotalAudience());
        map.put("totalViews", analytics.getTotalViews());
        map.put("totalAds", analytics.getTotalAds());
        map.put("avgViewSeconds", analytics.getAvgViewSeconds());
        
        // Age Distribution
        map.put("children", analytics.getChildren());
        map.put("teenagers", analytics.getTeenagers());
        map.put("youngAdults", analytics.getYoungAdults());
        map.put("midAged", analytics.getMidAged());
        map.put("seniors", analytics.getSeniors());
        
        // Gender Distribution
        map.put("male", analytics.getMale());
        map.put("female", analytics.getFemale());
        
        // Emotion Distribution
        map.put("neutral", analytics.getNeutral());
        map.put("serious", analytics.getSerious());
        map.put("happy", analytics.getHappy());
        map.put("surprised", analytics.getSurprised());
        
        return map;
    }
    
    /**
     * Map domain AdAnalytics to DTO map
     */
    private Map<String, Object> mapAdAnalytics(AdAnalytics analytics) {
        Map<String, Object> map = new HashMap<>();
        map.put("adName", analytics.getAdName());
        map.put("totalViewers", analytics.getTotalViewers());
        map.put("lookYes", analytics.getLookYes());
        map.put("lookNo", analytics.getLookNo());
        return map;
    }
}
