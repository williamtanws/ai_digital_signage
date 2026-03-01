package io.jeecloud.aidigitalsignage.digitalsignage.application.service;

import io.jeecloud.aidigitalsignage.digitalsignage.application.dto.*;
import io.jeecloud.aidigitalsignage.digitalsignage.application.port.in.GetDashboardOverviewUseCase;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard Query Service (CQRS - Query Side)
 * 
 * Handles read operations for dashboard data.
 * Implements use case from application layer.
 * Uses domain repositories (ports).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService implements GetDashboardOverviewUseCase {

    private static final Logger log = LoggerFactory.getLogger(DashboardQueryService.class);

    private final DashboardMetricsRepository metricsRepository;
    private final AdvertisementRepository advertisementRepository;
    private final SystemHealthRepository systemHealthRepository;
    private final ResearchMetricsRepository researchMetricsRepository;

    @Override
    public DashboardOverviewResponse getDashboardOverview() {
        log.debug("Fetching dashboard overview from domain repositories");

        DashboardMetrics metrics = metricsRepository.findCurrent()
                .orElseThrow(() -> new RuntimeException("No dashboard metrics found"));
        
        List<Advertisement> ads = advertisementRepository.findAllOrderedByViewers();

        // Fetch system health and research metrics (optional - may not be available yet)
        SystemHealthDto systemHealthDto = systemHealthRepository.findCurrent()
                .map(this::mapSystemHealth)
                .orElse(null);
        
        ResearchMetricsDto researchMetricsDto = researchMetricsRepository.findCurrent()
                .map(this::mapResearchMetrics)
                .orElse(null);

        return DashboardOverviewResponse.builder()
                .totalAudience(metrics.getTotalAudience())
                .totalViews(metrics.getTotalViews())
                .totalAds(metrics.getTotalAds())
                .avgViewSeconds(metrics.getAvgViewSeconds())
                .ageDistribution(mapAgeDistribution(metrics.getAgeDistribution()))
                .genderDistribution(mapGenderDistribution(metrics.getGenderDistribution()))
                .emotionDistribution(mapEmotionDistribution(metrics.getEmotionDistribution()))
                .adsPerformance(mapAdsPerformance(ads))
                .adsAttention(mapAdsAttention(ads))
                .systemHealth(systemHealthDto)
                .researchMetrics(researchMetricsDto)
                .build();
    }

    private AgeDistributionDto mapAgeDistribution(DashboardMetrics.AgeDistribution age) {
        return AgeDistributionDto.builder()
                .children(age.getChildren())
                .teenagers(age.getTeenagers())
                .youngAdults(age.getYoungAdults())
                .midAged(age.getMidAged())
                .seniors(age.getSeniors())
                .build();
    }

    private GenderDistributionDto mapGenderDistribution(DashboardMetrics.GenderDistribution gender) {
        return GenderDistributionDto.builder()
                .male(gender.getMale())
                .female(gender.getFemale())
                .build();
    }

    private EmotionDistributionDto mapEmotionDistribution(DashboardMetrics.EmotionDistribution emotion) {
        return EmotionDistributionDto.builder()
                .anger(emotion.getAnger())
                .contempt(emotion.getContempt())
                .disgust(emotion.getDisgust())
                .fear(emotion.getFear())
                .happiness(emotion.getHappiness())
                .neutral(emotion.getNeutral())
                .sadness(emotion.getSadness())
                .surprise(emotion.getSurprise())
                .build();
    }

    private List<AdsPerformanceDto> mapAdsPerformance(List<Advertisement> ads) {
        return ads.stream()
                .map(ad -> AdsPerformanceDto.builder()
                        .adName(ad.getAdName())
                        .totalViewers(ad.getTotalViewers())
                        .build())
                .collect(Collectors.toList());
    }

    private SystemHealthDto mapSystemHealth(SystemHealth health) {
        return SystemHealthDto.builder()
                .status(health.getStatus())
                .performance(mapPerformanceMetrics(health.getPerformance()))
                .environment(mapEnvironmentMetrics(health.getEnvironment()))
                .uptime(health.getUptime())
                .build();
    }

    private SystemHealthDto.PerformanceMetricsDto mapPerformanceMetrics(SystemHealth.PerformanceMetrics perf) {
        if (perf == null) return null;
        return SystemHealthDto.PerformanceMetricsDto.builder()
                .currentFps(perf.getCurrentFps())
                .avgFps(perf.getAvgFps())
                .minFps(perf.getMinFps())
                .maxFps(perf.getMaxFps())
                .currentCpuTemp(perf.getCurrentCpuTemp())
                .maxCpuTemp(perf.getMaxCpuTemp())
                .cpuThreshold(perf.getCpuThreshold())
                .build();
    }

    private SystemHealthDto.EnvironmentMetricsDto mapEnvironmentMetrics(SystemHealth.EnvironmentMetrics env) {
        if (env == null) return null;
        return SystemHealthDto.EnvironmentMetricsDto.builder()
                .temperatureCelsius(env.getTemperatureCelsius())
                .humidityPercent(env.getHumidityPercent())
                .pressureHpa(env.getPressureHpa())
                .gasResistanceOhms(env.getGasResistanceOhms())
                .noiseDb(env.getNoiseDb())
                .build();
    }

    private ResearchMetricsDto mapResearchMetrics(ResearchMetrics research) {
        return ResearchMetricsDto.builder()
                .faceDetection(mapFaceDetection(research.getFaceDetection()))
                .gazeQuality(mapGazeQuality(research.getGazeQuality()))
                .comparison(mapComparison(research.getComparison()))
                .build();
    }

    private ResearchMetricsDto.FaceDetectionMetricsDto mapFaceDetection(ResearchMetrics.FaceDetectionMetrics face) {
        if (face == null) return null;
        return ResearchMetricsDto.FaceDetectionMetricsDto.builder()
                .accuracy(face.getAccuracy())
                .confidence(face.getConfidence())
                .framesProcessed(face.getFramesProcessed())
                .facesDetected(face.getFacesDetected())
                .qualityRating(face.getQualityRating())
                .build();
    }

    private ResearchMetricsDto.GazeQualityMetricsDto mapGazeQuality(ResearchMetrics.GazeQualityMetrics gaze) {
        if (gaze == null) return null;
        return ResearchMetricsDto.GazeQualityMetricsDto.builder()
                .primaryMethodRate(gaze.getPrimaryMethodRate())
                .fallbackMethodRate(gaze.getFallbackMethodRate())
                .avgConfidence(gaze.getAvgConfidence())
                .qualityScore(gaze.getQualityScore())
                .recommendation(gaze.getRecommendation())
                .build();
    }

    private ResearchMetricsDto.ComparisonMetricsDto mapComparison(ResearchMetrics.ComparisonMetrics comp) {
        if (comp == null) return null;
        return ResearchMetricsDto.ComparisonMetricsDto.builder()
                .baseline(mapBaseline(comp.getBaseline()))
                .current(mapCurrent(comp.getCurrent()))
                .improvement(mapImprovement(comp.getImprovement()))
                .build();
    }

    private ResearchMetricsDto.ComparisonMetricsDto.BaselineDataDto mapBaseline(ResearchMetrics.ComparisonMetrics.BaselineData baseline) {
        if (baseline == null) return null;
        return ResearchMetricsDto.ComparisonMetricsDto.BaselineDataDto.builder()
                .condition(baseline.getCondition())
                .avgEngagement(baseline.getAvgEngagement())
                .period(baseline.getPeriod())
                .build();
    }

    private ResearchMetricsDto.ComparisonMetricsDto.CurrentDataDto mapCurrent(ResearchMetrics.ComparisonMetrics.CurrentData current) {
        if (current == null) return null;
        return ResearchMetricsDto.ComparisonMetricsDto.CurrentDataDto.builder()
                .condition(current.getCondition())
                .avgEngagement(current.getAvgEngagement())
                .period(current.getPeriod())
                .build();
    }

    private ResearchMetricsDto.ComparisonMetricsDto.ImprovementDataDto mapImprovement(ResearchMetrics.ComparisonMetrics.ImprovementData improvement) {
        if (improvement == null) return null;
        return ResearchMetricsDto.ComparisonMetricsDto.ImprovementDataDto.builder()
                .absolute(improvement.getAbsolute())
                .percentage(improvement.getPercentage())
                .significant(improvement.getSignificant())
                .build();
    }

    private List<AdsAttentionDto> mapAdsAttention(List<Advertisement> ads) {
        return ads.stream()
                .map(ad -> AdsAttentionDto.builder()
                        .adName(ad.getAdName())
                        .lookYes(ad.getLookYes())
                        .lookNo(ad.getLookNo())
                        .build())
                .collect(Collectors.toList());
    }
}
