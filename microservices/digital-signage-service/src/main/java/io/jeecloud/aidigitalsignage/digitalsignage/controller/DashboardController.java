package io.jeecloud.aidigitalsignage.digitalsignage.controller;

import io.jeecloud.aidigitalsignage.digitalsignage.dto.DashboardOverviewResponse;
import io.jeecloud.aidigitalsignage.digitalsignage.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard REST Controller
 * 
 * Exposes a single API endpoint for retrieving dashboard data.
 * All dashboard metrics are returned in one response to minimize
 * API calls and simplify frontend integration.
 * 
 * Endpoint:
 * GET /api/dashboard/overview - Returns complete dashboard data
 * 
 * Response includes:
 * - Top-level KPIs (audience, views, ads, average time)
 * - Demographic breakdowns (age, gender)
 * - Emotional analysis (expressions detected)
 * - Advertisement performance (viewer counts)
 * - Advertisement attention (engagement metrics)
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Get Dashboard Overview
     * 
     * Returns comprehensive dashboard data in a single API call.
     * This endpoint is designed for frontend dashboards that need
     * to display all metrics simultaneously.
     * 
     * Usage:
     * - Frontend calls this endpoint on dashboard load
     * - All charts and KPIs are rendered from this response
     * - No authentication required (prototype only)
     * 
     * @return Complete dashboard data with all metrics
     */
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getDashboardOverview() {
        log.info("Dashboard overview requested");

        DashboardOverviewResponse response = dashboardService.getDashboardOverview();

        log.debug("Dashboard data generated - Audience: {}, Views: {}, Ads: {}",
                response.getTotalAudience(),
                response.getTotalViews(),
                response.getTotalAds());

        return ResponseEntity.ok(response);
    }
}
