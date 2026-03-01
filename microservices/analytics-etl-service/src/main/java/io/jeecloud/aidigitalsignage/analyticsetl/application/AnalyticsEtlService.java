package io.jeecloud.aidigitalsignage.analyticsetl.application;

import io.jeecloud.aidigitalsignage.analyticsetl.application.dto.ResearchMetricsDto;
import io.jeecloud.aidigitalsignage.analyticsetl.application.dto.SystemHealthDto;
import io.jeecloud.aidigitalsignage.analyticsetl.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ETL Service (Application Layer)
 * 
 * Orchestrates the Extract-Transform-Load process:
 * 1. EXTRACT: Read gaze events from TDengine (incremental)
 * 2. TRANSFORM: Aggregate into analytics
 * 3. LOAD: Insert into SQLite
 * 
 * This is the core use case of the analytics pipeline.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEtlService {
    
    private final GazeEventRepository gazeEventRepository;
    private final AnalyticsRepository analyticsRepository;
    private final EtlMetadataRepository etlMetadataRepository;
    
    /**
     * Execute incremental ETL pipeline
     * 
     * Steps:
     * 1. Check last processed timestamp
     * 2. Extract NEW gaze events from TDengine (only after last timestamp)
     * 3. Transform into dashboard and ad analytics
     * 4. Load into SQLite database
     * 5. Update last processed timestamp
     */
    public void executeEtl() {
        log.info("=== Starting Analytics ETL Process (Incremental) ===");
        
        try {
            // Step 1: Check last processed timestamp
            Optional<Instant> lastProcessedOpt = etlMetadataRepository.getLastProcessedTimestamp();
            
            List<GazeEvent> sessionEvents;
            List<GazeEvent> heartbeatEvents;
            
            if (lastProcessedOpt.isPresent()) {
                // Incremental mode: Only fetch new events
                Instant lastProcessed = lastProcessedOpt.get();
                log.info("[EXTRACT] Incremental mode - fetching events after {}", lastProcessed);
                sessionEvents = gazeEventRepository.findSessionEndEventsAfter(lastProcessed);
                heartbeatEvents = gazeEventRepository.findHeartbeatEventsAfter(lastProcessed);
                log.info("[EXTRACT] Found {} NEW session end events, {} heartbeat events", 
                        sessionEvents.size(), heartbeatEvents.size());
            } else {
                // First run: Fetch all events
                log.info("[EXTRACT] First run - fetching ALL events from TDengine");
                sessionEvents = gazeEventRepository.findAllSessionEndEvents();
                heartbeatEvents = gazeEventRepository.findAllHeartbeatEvents();
                log.info("[EXTRACT] Found {} session end events, {} heartbeat events", 
                        sessionEvents.size(), heartbeatEvents.size());
            }
            
            if (sessionEvents.isEmpty() && heartbeatEvents.isEmpty()) {
                log.info("[ETL] No new events found, skipping transformation");
                return;
            }
            
            // Step 2: TRANSFORM - Aggregate events into analytics
            DashboardAnalytics newDashboardAnalytics = null;
            List<AdAnalytics> newAdAnalyticsList = List.of();
            
            if (!sessionEvents.isEmpty()) {
                log.info("[TRANSFORM] Aggregating {} session events into analytics...", sessionEvents.size());
                newDashboardAnalytics = transformToDashboardAnalytics(sessionEvents);
                newAdAnalyticsList = transformToAdAnalytics(sessionEvents);
                log.info("[TRANSFORM] Created dashboard analytics and {} ad analytics", newAdAnalyticsList.size());
            }
            
            // Transform heartbeat events into system health and research metrics
            SystemHealthDto systemHealthDto = null;
            ResearchMetricsDto researchMetricsDto = null;
            
            if (!heartbeatEvents.isEmpty()) {
                log.info("[TRANSFORM] Transforming {} heartbeat events into system health metrics", heartbeatEvents.size());
                systemHealthDto = transformToSystemHealth(heartbeatEvents);
                researchMetricsDto = transformToResearchMetrics(sessionEvents, heartbeatEvents);
            }
            
            // Step 3: LOAD - Merge with existing and save (accumulate mode)
            log.info("[LOAD] Fetching existing analytics...");
            Optional<DashboardAnalytics> existingDashboard = analyticsRepository.getExistingDashboardAnalytics();
            List<AdAnalytics> existingAds = analyticsRepository.getExistingAdAnalytics();
            
            DashboardAnalytics mergedDashboard = null;
            List<AdAnalytics> mergedAds = List.of();
            
            if (!sessionEvents.isEmpty()) {
                // Merge dashboard analytics
                mergedDashboard = mergeDashboardAnalytics(existingDashboard, newDashboardAnalytics);
                
                // Merge ad analytics
                mergedAds = mergeAdAnalytics(existingAds, newAdAnalyticsList);
                
                log.info("[LOAD] Saving merged dashboard analytics (accumulated)...");
                analyticsRepository.saveDashboardAnalytics(mergedDashboard);
                
                log.info("[LOAD] Saving {} merged advertisement analytics with system health/research metrics...", mergedAds.size());
                analyticsRepository.saveAdAnalytics(mergedAds, systemHealthDto, researchMetricsDto);
                
                logSummary(mergedDashboard, mergedAds);
            } else if (systemHealthDto != null || researchMetricsDto != null) {
                // Only heartbeat events - reuse existing dashboard if available
                if (existingDashboard.isPresent()) {
                    log.info("[LOAD] Re-saving existing dashboard with updated system health/research metrics...");
                    analyticsRepository.saveDashboardAnalytics(existingDashboard.get());
                }
                log.info("[LOAD] Saving system health and research metrics...");
                analyticsRepository.saveAdAnalytics(existingAds, systemHealthDto, researchMetricsDto);
            }
            
            // Step 4: Update last processed timestamp
            Instant latestTimestamp = Instant.now();
            
            if (!sessionEvents.isEmpty()) {
                latestTimestamp = sessionEvents.stream()
                        .map(GazeEvent::getTimestamp)
                        .max(Instant::compareTo)
                        .orElse(Instant.now());
            } else if (!heartbeatEvents.isEmpty()) {
                latestTimestamp = heartbeatEvents.stream()
                        .map(GazeEvent::getTimestamp)
                        .max(Instant::compareTo)
                        .orElse(Instant.now());
            }
            
            int totalEventCount = sessionEvents.size() + heartbeatEvents.size();
            etlMetadataRepository.updateLastProcessedTimestamp(latestTimestamp, totalEventCount);
            
            log.info("=== Analytics ETL Process Completed Successfully ===");
            
        } catch (Exception e) {
            log.error("[ETL] Failed to execute ETL process", e);
            throw new RuntimeException("ETL process failed", e);
        }
    }
    
    /**
     * TRANSFORM: Aggregate session events into dashboard analytics
     * 
     * Calculations:
     * - Total audience: unique viewers
     * - Total views: sum of all viewing sessions
     * - Avg view seconds: average session duration
     * - Demographics: count by age group, gender, emotion
     */
    private DashboardAnalytics transformToDashboardAnalytics(List<GazeEvent> events) {
        log.debug("[TRANSFORM] Processing {} events for dashboard analytics", events.size());
        
        // Count unique viewers
        int uniqueViewers = (int) events.stream()
                .map(GazeEvent::getViewerId)
                .distinct()
                .count();
        
        // Calculate total views (all sessions)
        int totalViews = events.size();
        
        // Calculate average view time
        double avgViewSeconds = events.stream()
                .filter(e -> e.getSessionDuration() != null)
                .mapToDouble(GazeEvent::getSessionDuration)
                .average()
                .orElse(0.0);
        
        // Aggregate age distribution
        Map<String, Long> ageGroups = categorizeByAgeGroup(events);
        
        // Aggregate gender distribution
        Map<String, Long> genderCounts = events.stream()
                .filter(e -> e.getGender() != null && !e.getGender().isEmpty())
                .collect(Collectors.groupingBy(GazeEvent::getGender, Collectors.counting()));
        
        // Aggregate emotion distribution
        Map<String, Long> emotionCounts = events.stream()
                .filter(e -> e.getEmotion() != null && !e.getEmotion().isEmpty())
                .collect(Collectors.groupingBy(GazeEvent::getEmotion, Collectors.counting()));
        
        // Count unique advertisements (if any)
        int totalAds = (int) events.stream()
                .map(GazeEvent::getAdName)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        
        // Default to 12 if no ad data (matches mock data)
        totalAds = totalAds > 0 ? totalAds : 12;
        
        return DashboardAnalytics.builder()
                .totalAudience(uniqueViewers)
                .totalViews(totalViews)
                .totalAds(totalAds)
                .avgViewSeconds(avgViewSeconds)
                // Age distribution
                .children(ageGroups.getOrDefault("children", 0L).intValue())
                .teenagers(ageGroups.getOrDefault("teenagers", 0L).intValue())
                .youngAdults(ageGroups.getOrDefault("youngAdults", 0L).intValue())
                .midAged(ageGroups.getOrDefault("midAged", 0L).intValue())
                .seniors(ageGroups.getOrDefault("seniors", 0L).intValue())
                // Gender distribution
                .male(genderCounts.getOrDefault("Male", 0L).intValue())
                .female(genderCounts.getOrDefault("Female", 0L).intValue())
                // Emotion distribution (FER2013 - 8 emotions)
                .anger(emotionCounts.getOrDefault("anger", 0L).intValue())
                .contempt(emotionCounts.getOrDefault("contempt", 0L).intValue())
                .disgust(emotionCounts.getOrDefault("disgust", 0L).intValue())
                .fear(emotionCounts.getOrDefault("fear", 0L).intValue())
                .happiness(emotionCounts.getOrDefault("happiness", 0L).intValue())
                .neutral(emotionCounts.getOrDefault("neutral", 0L).intValue())
                .sadness(emotionCounts.getOrDefault("sadness", 0L).intValue())
                .surprise(emotionCounts.getOrDefault("surprise", 0L).intValue())
                .build();
    }
    
    /**
     * Helper: Categorize ages into groups
     * 
     * Age Groups:
     * - Children: 0-12
     * - Teenagers: 13-19
     * - Young Adults: 20-35
     * - Mid-Aged: 36-55
     * - Seniors: 56+
     */
    private Map<String, Long> categorizeByAgeGroup(List<GazeEvent> events) {
        Map<String, Long> ageGroups = new HashMap<>();
        ageGroups.put("children", 0L);
        ageGroups.put("teenagers", 0L);
        ageGroups.put("youngAdults", 0L);
        ageGroups.put("midAged", 0L);
        ageGroups.put("seniors", 0L);
        
        for (GazeEvent event : events) {
            Integer age = event.getAge();
            if (age == null || age == 0) continue;
            
            if (age <= 12) {
                ageGroups.merge("children", 1L, Long::sum);
            } else if (age <= 19) {
                ageGroups.merge("teenagers", 1L, Long::sum);
            } else if (age <= 35) {
                ageGroups.merge("youngAdults", 1L, Long::sum);
            } else if (age <= 55) {
                ageGroups.merge("midAged", 1L, Long::sum);
            } else {
                ageGroups.merge("seniors", 1L, Long::sum);
            }
        }
        
        return ageGroups;
    }
    
    /**
     * TRANSFORM: Aggregate session events into advertisement analytics
     * 
     * For each advertisement:
     * - Count total viewers
     * - Count engaged viewers (high engagement rate = looked at ad)
     * - Count non-engaged viewers
     */
    private List<AdAnalytics> transformToAdAnalytics(List<GazeEvent> events) {
        log.debug("[TRANSFORM] Processing events for advertisement analytics");
        
        // Group events by advertisement
        Map<String, List<GazeEvent>> eventsByAd = events.stream()
                .filter(e -> e.getAdName() != null && !e.getAdName().isEmpty())
                .collect(Collectors.groupingBy(GazeEvent::getAdName));
        
        // If no ad data, return empty list
        if (eventsByAd.isEmpty()) {
            log.debug("[TRANSFORM] No advertisement data found in events");
            return Collections.emptyList();
        }
        
        // Calculate analytics per ad
        List<AdAnalytics> adAnalyticsList = new ArrayList<>();
        
        for (Map.Entry<String, List<GazeEvent>> entry : eventsByAd.entrySet()) {
            String adName = entry.getKey();
            List<GazeEvent> adEvents = entry.getValue();
            
            int totalViewers = adEvents.size();
            
            // Viewers with high engagement (>= 0.5) are considered "looking"
            int lookYes = (int) adEvents.stream()
                    .filter(e -> e.getEngagementRate() != null && e.getEngagementRate() >= 0.5)
                    .count();
            
            int lookNo = totalViewers - lookYes;
            
            AdAnalytics adAnalytics = AdAnalytics.builder()
                    .adName(adName)
                    .totalViewers(totalViewers)
                    .lookYes(lookYes)
                    .lookNo(lookNo)
                    .build();
            
            adAnalyticsList.add(adAnalytics);
        }
        
        // Sort by total viewers descending
        adAnalyticsList.sort((a, b) -> Integer.compare(b.getTotalViewers(), a.getTotalViewers()));
        
        return adAnalyticsList;
    }
    
    /**
     * MERGE: Combine existing dashboard analytics with new data
     * 
     * Accumulation strategy:
     * - Counts: Add new to existing
     * - Averages: Weighted average based on view counts
     */
    private DashboardAnalytics mergeDashboardAnalytics(Optional<DashboardAnalytics> existingOpt, DashboardAnalytics newAnalytics) {
        if (existingOpt.isEmpty()) {
            log.debug("[MERGE] No existing analytics, using new data only");
            return newAnalytics;
        }
        
        DashboardAnalytics existing = existingOpt.get();
        
        // Accumulate counts
        int totalAudience = existing.getTotalAudience() + newAnalytics.getTotalAudience();
        int totalViews = existing.getTotalViews() + newAnalytics.getTotalViews();
        int totalAds = Math.max(existing.getTotalAds(), newAnalytics.getTotalAds());
        
        // Weighted average for view time
        double weightedAvg = 0.0;
        if (totalViews > 0) {
            double existingWeight = existing.getTotalViews() * existing.getAvgViewSeconds();
            double newWeight = newAnalytics.getTotalViews() * newAnalytics.getAvgViewSeconds();
            weightedAvg = (existingWeight + newWeight) / totalViews;
        }
        
        log.info("[MERGE] Dashboard: {} + {} = {} viewers, {} + {} = {} views",
                existing.getTotalAudience(), newAnalytics.getTotalAudience(), totalAudience,
                existing.getTotalViews(), newAnalytics.getTotalViews(), totalViews);
        
        return DashboardAnalytics.builder()
                .totalAudience(totalAudience)
                .totalViews(totalViews)
                .totalAds(totalAds)
                .avgViewSeconds(weightedAvg)
                // Accumulate age distribution
                .children(existing.getChildren() + newAnalytics.getChildren())
                .teenagers(existing.getTeenagers() + newAnalytics.getTeenagers())
                .youngAdults(existing.getYoungAdults() + newAnalytics.getYoungAdults())
                .midAged(existing.getMidAged() + newAnalytics.getMidAged())
                .seniors(existing.getSeniors() + newAnalytics.getSeniors())
                // Accumulate gender distribution
                .male(existing.getMale() + newAnalytics.getMale())
                .female(existing.getFemale() + newAnalytics.getFemale())
                // Accumulate emotion distribution (FER2013 - 8 emotions)
                .anger(existing.getAnger() + newAnalytics.getAnger())
                .contempt(existing.getContempt() + newAnalytics.getContempt())
                .disgust(existing.getDisgust() + newAnalytics.getDisgust())
                .fear(existing.getFear() + newAnalytics.getFear())
                .happiness(existing.getHappiness() + newAnalytics.getHappiness())
                .neutral(existing.getNeutral() + newAnalytics.getNeutral())
                .sadness(existing.getSadness() + newAnalytics.getSadness())
                .surprise(existing.getSurprise() + newAnalytics.getSurprise())
                .build();
    }
    
    /**
     * MERGE: Combine existing ad analytics with new data
     * 
     * For each ad:
     * - If exists in both: accumulate counts
     * - If only in new: add to result
     * - If only in existing: keep in result
     */
    private List<AdAnalytics> mergeAdAnalytics(List<AdAnalytics> existing, List<AdAnalytics> newAnalytics) {
        // Build map of existing ads
        Map<String, AdAnalytics> existingMap = existing.stream()
                .collect(Collectors.toMap(AdAnalytics::getAdName, a -> a));
        
        // Merge
        List<AdAnalytics> result = new ArrayList<>();
        Set<String> processedAds = new HashSet<>();
        
        for (AdAnalytics newAd : newAnalytics) {
            String adName = newAd.getAdName();
            processedAds.add(adName);
            
            if (existingMap.containsKey(adName)) {
                AdAnalytics existingAd = existingMap.get(adName);
                result.add(AdAnalytics.builder()
                        .adName(adName)
                        .totalViewers(existingAd.getTotalViewers() + newAd.getTotalViewers())
                        .lookYes(existingAd.getLookYes() + newAd.getLookYes())
                        .lookNo(existingAd.getLookNo() + newAd.getLookNo())
                        .build());
                log.debug("[MERGE] Ad '{}': {} + {} = {} viewers", 
                        adName, existingAd.getTotalViewers(), newAd.getTotalViewers(),
                        existingAd.getTotalViewers() + newAd.getTotalViewers());
            } else {
                result.add(newAd);
            }
        }
        
        // Add any existing ads not in new data
        for (AdAnalytics existingAd : existing) {
            if (!processedAds.contains(existingAd.getAdName())) {
                result.add(existingAd);
            }
        }
        
        // Sort by total viewers descending
        result.sort((a, b) -> Integer.compare(b.getTotalViewers(), a.getTotalViewers()));
        
        log.info("[MERGE] Merged {} ad analytics", result.size());
        return result;
    }
    
    /**
     * Log ETL summary
     */
    private void logSummary(DashboardAnalytics dashboard, List<AdAnalytics> ads) {
        log.info("=== ETL Summary ===");
        log.info("Total Audience: {}", dashboard.getTotalAudience());
        log.info("Total Views: {}", dashboard.getTotalViews());
        log.info("Avg View Time: {:.2f} seconds", dashboard.getAvgViewSeconds());
        log.info("Total Ads: {}", dashboard.getTotalAds());
        log.info("Ad Analytics: {} ads processed", ads.size());
        log.info("==================");
    }
    
    /**
     * TRANSFORM: Convert heartbeat events into SystemHealthDto
     */
    private SystemHealthDto transformToSystemHealth(List<GazeEvent> heartbeatEvents) {
        if (heartbeatEvents.isEmpty()) {
            return null;
        }
        
        // Calculate performance metrics from heartbeat events
        DoubleSummaryStatistics fpsStats = heartbeatEvents.stream()
                .map(GazeEvent::getFps)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        
        DoubleSummaryStatistics cpuTempStats = heartbeatEvents.stream()
                .map(GazeEvent::getCpuTemp)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        
        // Get most recent heartbeat for current values
        GazeEvent latestHeartbeat = heartbeatEvents.get(heartbeatEvents.size() - 1);
        
        SystemHealthDto.PerformanceMetricsDto performance = SystemHealthDto.PerformanceMetricsDto.builder()
                .currentFps(latestHeartbeat.getFps())
                .avgFps(fpsStats.getCount() > 0 ? fpsStats.getAverage() : null)
                .minFps(fpsStats.getCount() > 0 ? fpsStats.getMin() : null)
                .maxFps(fpsStats.getCount() > 0 ? fpsStats.getMax() : null)
                .currentCpuTemp(latestHeartbeat.getCpuTemp())
                .maxCpuTemp(cpuTempStats.getCount() > 0 ? cpuTempStats.getMax() : null)
                .cpuThreshold(70.0) // Standard threshold for Raspberry Pi
                .build();
        
        // Calculate environment metrics
        DoubleSummaryStatistics tempStats = heartbeatEvents.stream()
                .map(GazeEvent::getTemperature)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        
        DoubleSummaryStatistics humidityStats = heartbeatEvents.stream()
                .map(GazeEvent::getHumidity)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        
        DoubleSummaryStatistics noiseStats = heartbeatEvents.stream()
                .map(GazeEvent::getNoise)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        
        SystemHealthDto.EnvironmentMetricsDto environment = SystemHealthDto.EnvironmentMetricsDto.builder()
                .temperatureCelsius(tempStats.getCount() > 0 ? tempStats.getAverage() : null)
                .humidityPercent(humidityStats.getCount() > 0 ? humidityStats.getAverage() : null)
                .pressureHpa(latestHeartbeat.getPressure())
                .gasResistanceOhms(latestHeartbeat.getGasResistance())
                .noiseDb(noiseStats.getCount() > 0 ? noiseStats.getAverage() : null)
                .build();
        
        // Determine system status based on FPS and CPU temperature
        String status = "HEALTHY";
        if (cpuTempStats.getMax() > 70.0) {
            status = "CRITICAL";
        } else if (cpuTempStats.getMax() > 65.0 || fpsStats.getAverage() < 5.0) {
            status = "WARNING";
        }
        
        return SystemHealthDto.builder()
                .status(status)
                .performance(performance)
                .environment(environment)
                .uptime(latestHeartbeat.getUptime())
                .build();
    }
    
    /**
     * TRANSFORM: Convert events into ResearchMetricsDto
     * 
     * Face detection metrics from heartbeat events (instantaneous diagnostics aggregated)
     * Gaze quality metrics from heartbeat events (keypoint validity, solvePnP success)
     * Comparison metrics from baseline data (requires 2-week collection - Phase 3)
     */
    private ResearchMetricsDto transformToResearchMetrics(List<GazeEvent> sessionEvents, List<GazeEvent> heartbeatEvents) {
        if (heartbeatEvents.isEmpty()) {
            return null;
        }
        
        // Calculate face detection accuracy from heartbeat diagnostics
        // Each heartbeat contains instantaneous face detection stats
        long totalHeartbeats = heartbeatEvents.stream()
                .map(GazeEvent::getFacesInFrame)
                .filter(Objects::nonNull)
                .count();
        
        long heartbeatsWithFaces = heartbeatEvents.stream()
                .map(GazeEvent::getFacesInFrame)
                .filter(Objects::nonNull)
                .filter(count -> count > 0)
                .count();
        
        // Accuracy: percentage of heartbeats that detected at least one face
        double accuracy = totalHeartbeats > 0 ? (heartbeatsWithFaces * 100.0 / totalHeartbeats) : 0.0;
        
        // Average face confidence from heartbeats (only when face was detected)
        double avgConfidence = heartbeatEvents.stream()
                .map(GazeEvent::getFaceConfidence)
                .filter(Objects::nonNull)
                .filter(conf -> conf > 0.0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // Total faces detected across all heartbeats
        int totalFacesDetected = heartbeatEvents.stream()
                .map(GazeEvent::getFacesInFrame)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        
        ResearchMetricsDto.FaceDetectionMetricsDto faceDetection = ResearchMetricsDto.FaceDetectionMetricsDto.builder()
                .accuracy(accuracy)
                .confidence(avgConfidence)
                .framesProcessed((int) totalHeartbeats)
                .facesDetected(totalFacesDetected)
                .build();
        
        // Calculate gaze quality metrics from heartbeat diagnostics
        double avgKptsValid = heartbeatEvents.stream()
                .map(GazeEvent::getKptsValidPercent)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        double avgSolvepnpSuccess = heartbeatEvents.stream()
                .map(GazeEvent::getSolvepnpSuccessPercent)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        double avgFallback = heartbeatEvents.stream()
                .map(GazeEvent::getFallbackPercent)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        ResearchMetricsDto.GazeQualityMetricsDto gazeQuality = ResearchMetricsDto.GazeQualityMetricsDto.builder()
                .kptsValidPercent(avgKptsValid)
                .solvepnpSuccessPercent(avgSolvepnpSuccess)
                .fallbackPercent(avgFallback)
                .build();
        
        // TODO: Comparison metrics require baseline data collection
        // Phase 3: Collect 2 weeks static signage + 2 weeks dynamic signage
        // Calculate statistical significance with t-test
        
        return ResearchMetricsDto.builder()
                .faceDetection(faceDetection)
                .gazeQuality(gazeQuality)
                .comparison(null) // Will be added in Phase 3 (baseline data collection)
                .build();
    }
}
