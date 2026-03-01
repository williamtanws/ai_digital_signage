package io.jeecloud.aidigitalsignage.analyticsetl.infrastructure.restclient;

import io.jeecloud.aidigitalsignage.analyticsetl.application.dto.ResearchMetricsDto;
import io.jeecloud.aidigitalsignage.analyticsetl.application.dto.SystemHealthDto;
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
import java.util.Optional;
import java.util.ArrayList;
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
     * Get existing dashboard analytics from the backend for accumulation
     */
    @Override
    @SuppressWarnings("unchecked")
    public Optional<DashboardAnalytics> getExistingDashboardAnalytics() {
        try {
            String url = digitalSignageServiceUrl + "/api/dashboard/overview";
            log.debug("Fetching existing analytics from: {}", url);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null || response.isEmpty()) {
                return Optional.empty();
            }
            
            // Parse age distribution
            Map<String, Object> ageDist = (Map<String, Object>) response.get("ageDistribution");
            Map<String, Object> genderDist = (Map<String, Object>) response.get("genderDistribution");
            Map<String, Object> emotionDist = (Map<String, Object>) response.get("emotionDistribution");
            
            DashboardAnalytics existing = DashboardAnalytics.builder()
                    .totalAudience(getIntValue(response, "totalAudience"))
                    .totalViews(getIntValue(response, "totalViews"))
                    .totalAds(getIntValue(response, "totalAds"))
                    .avgViewSeconds(getDoubleValue(response, "avgViewSeconds"))
                    .children(ageDist != null ? getIntValue(ageDist, "children") : 0)
                    .teenagers(ageDist != null ? getIntValue(ageDist, "teenagers") : 0)
                    .youngAdults(ageDist != null ? getIntValue(ageDist, "youngAdults") : 0)
                    .midAged(ageDist != null ? getIntValue(ageDist, "midAged") : 0)
                    .seniors(ageDist != null ? getIntValue(ageDist, "seniors") : 0)
                    .male(genderDist != null ? getIntValue(genderDist, "male") : 0)
                    .female(genderDist != null ? getIntValue(genderDist, "female") : 0)
                    .anger(emotionDist != null ? getIntValue(emotionDist, "anger") : 0)
                    .contempt(emotionDist != null ? getIntValue(emotionDist, "contempt") : 0)
                    .disgust(emotionDist != null ? getIntValue(emotionDist, "disgust") : 0)
                    .fear(emotionDist != null ? getIntValue(emotionDist, "fear") : 0)
                    .happiness(emotionDist != null ? getIntValue(emotionDist, "happiness") : 0)
                    .neutral(emotionDist != null ? getIntValue(emotionDist, "neutral") : 0)
                    .sadness(emotionDist != null ? getIntValue(emotionDist, "sadness") : 0)
                    .surprise(emotionDist != null ? getIntValue(emotionDist, "surprise") : 0)
                    .build();
            
            log.debug("Loaded existing analytics: {} viewers, {} views", 
                    existing.getTotalAudience(), existing.getTotalViews());
            return Optional.of(existing);
            
        } catch (Exception e) {
            log.warn("Could not fetch existing analytics (may be first run): {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get existing ad analytics from the backend for accumulation
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<AdAnalytics> getExistingAdAnalytics() {
        try {
            String url = digitalSignageServiceUrl + "/api/dashboard/overview";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null) {
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> adsPerformance = (List<Map<String, Object>>) response.get("adsPerformance");
            List<Map<String, Object>> adsAttention = (List<Map<String, Object>>) response.get("adsAttention");
            
            if (adsPerformance == null || adsPerformance.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Build map of attention data for lookup
            Map<String, int[]> attentionMap = new HashMap<>();
            if (adsAttention != null) {
                for (Map<String, Object> att : adsAttention) {
                    String adName = (String) att.get("adName");
                    attentionMap.put(adName, new int[]{
                            getIntValue(att, "lookYes"),
                            getIntValue(att, "lookNo")
                    });
                }
            }
            
            List<AdAnalytics> result = new ArrayList<>();
            for (Map<String, Object> perf : adsPerformance) {
                String adName = (String) perf.get("adName");
                int[] attention = attentionMap.getOrDefault(adName, new int[]{0, 0});
                
                result.add(AdAnalytics.builder()
                        .adName(adName)
                        .totalViewers(getIntValue(perf, "totalViewers"))
                        .lookYes(attention[0])
                        .lookNo(attention[1])
                        .build());
            }
            
            log.debug("Loaded {} existing ad analytics", result.size());
            return result;
            
        } catch (Exception e) {
            log.warn("Could not fetch existing ad analytics: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private int getIntValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }
    
    private double getDoubleValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return 0.0;
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
     * This method sends dashboard, ad analytics, system health, and research metrics together.
     */
    @Override
    public void saveAdAnalytics(List<AdAnalytics> adAnalyticsList, SystemHealthDto systemHealth, ResearchMetricsDto researchMetrics) {
        try {
            String url = digitalSignageServiceUrl + "/api/analytics/update";
            
            // Build request payload with dashboard, ads, system health, and research metrics
            Map<String, Object> request = buildUpdateRequest(currentDashboardAnalytics, adAnalyticsList, systemHealth, researchMetrics);
            
            log.info("Sending analytics update to digital-signage-service: {}", url);
            log.debug("Payload: dashboard metrics + {} ad analytics + system health + research metrics", adAnalyticsList.size());
            
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
    private Map<String, Object> buildUpdateRequest(DashboardAnalytics dashboard, List<AdAnalytics> adAnalyticsList, 
                                                   SystemHealthDto systemHealth, ResearchMetricsDto researchMetrics) {
        Map<String, Object> request = new HashMap<>();
        
        // Map dashboard metrics (only if available)
        if (dashboard != null) {
            request.put("dashboardMetrics", mapDashboardAnalytics(dashboard));
        }
        
        // Map ad analytics
        List<Map<String, Object>> adMetrics = adAnalyticsList.stream()
                .map(this::mapAdAnalytics)
                .collect(Collectors.toList());
        if (!adMetrics.isEmpty()) {
            request.put("adMetrics", adMetrics);
        }
        
        // Add system health if available
        if (systemHealth != null) {
            request.put("systemHealth", systemHealth);
            log.debug("Including system health metrics (FPS: {}, CPU: {}°C)", 
                    systemHealth.getPerformance() != null ? systemHealth.getPerformance().getCurrentFps() : "N/A",
                    systemHealth.getPerformance() != null ? systemHealth.getPerformance().getCurrentCpuTemp() : "N/A");
        }
        
        // Add research metrics if available
        if (researchMetrics != null) {
            request.put("researchMetrics", researchMetrics);
            log.debug("Including research metrics (Face detection: {}%, Gaze quality: {}% valid kpts)",
                    researchMetrics.getFaceDetection() != null ? researchMetrics.getFaceDetection().getAccuracy() : "N/A",
                    researchMetrics.getGazeQuality() != null ? researchMetrics.getGazeQuality().getKptsValidPercent() : "N/A");
        }
        
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
        map.put("anger", analytics.getAnger());
        map.put("contempt", analytics.getContempt());
        map.put("disgust", analytics.getDisgust());
        map.put("fear", analytics.getFear());
        map.put("happiness", analytics.getHappiness());
        map.put("neutral", analytics.getNeutral());
        map.put("sadness", analytics.getSadness());
        map.put("surprise", analytics.getSurprise());
        
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
