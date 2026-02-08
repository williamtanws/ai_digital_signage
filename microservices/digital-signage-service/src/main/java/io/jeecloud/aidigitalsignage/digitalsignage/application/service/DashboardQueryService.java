package io.jeecloud.aidigitalsignage.digitalsignage.application.service;

import io.jeecloud.aidigitalsignage.digitalsignage.application.dto.*;
import io.jeecloud.aidigitalsignage.digitalsignage.application.port.in.GetDashboardOverviewUseCase;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.Advertisement;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.AdvertisementRepository;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.DashboardMetrics;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.DashboardMetricsRepository;
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

    @Override
    public DashboardOverviewResponse getDashboardOverview() {
        log.debug("Fetching dashboard overview from domain repositories");

        DashboardMetrics metrics = metricsRepository.findCurrent()
                .orElseThrow(() -> new RuntimeException("No dashboard metrics found"));

        List<Advertisement> ads = advertisementRepository.findAllOrderedByViewers();

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
                .neutral(emotion.getNeutral())
                .serious(emotion.getSerious())
                .happy(emotion.getHappy())
                .surprised(emotion.getSurprised())
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
