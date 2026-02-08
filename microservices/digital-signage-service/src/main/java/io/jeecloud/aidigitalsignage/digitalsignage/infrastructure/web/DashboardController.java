package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.web;

import io.jeecloud.aidigitalsignage.digitalsignage.application.dto.DashboardOverviewResponse;
import io.jeecloud.aidigitalsignage.digitalsignage.application.port.in.GetDashboardOverviewUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard REST Controller (Infrastructure Layer - Primary Adapter)
 * 
 * Exposes HTTP endpoints for dashboard data.
 * Uses application layer use cases (input ports).
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Digital Signage Analytics Dashboard API")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final GetDashboardOverviewUseCase getDashboardOverviewUseCase;

    /**
     * GET /api/dashboard/overview
     * 
     * Retrieves complete dashboard overview including:
     * - KPIs (audience, views, ads, avg view time)
     * - Demographics (age, gender)
     * - Emotions (facial expressions)
     * - Advertisement performance and attention metrics
     * 
     * @return Dashboard data wrapped in ResponseEntity
     */
    @Operation(
            summary = "Get Dashboard Overview",
            description = """
                    Retrieves comprehensive analytics dashboard data including:
                    
                    **KPIs:**
                    - Total unique audience count
                    - Total views/sessions
                    - Total advertisements tracked
                    - Average view time in seconds
                    
                    **Demographics:**
                    - Age distribution (children, teenagers, young adults, mid-aged, seniors)
                    - Gender distribution (male, female)
                    
                    **Emotions:**
                    - Emotion detection metrics (neutral, serious, happy, surprised)
                    
                    **Advertisement Performance:**
                    - Per-ad viewer counts
                    - Engagement metrics (viewers looking vs not looking)
                    
                    Data is aggregated from TDengine time-series database via ETL pipeline.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved dashboard overview",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DashboardOverviewResponse.class),
                            examples = @ExampleObject(
                                    name = "Dashboard Overview Example",
                                    value = """
                                            {
                                              "totalAudience": 57,
                                              "totalViews": 57,
                                              "totalAds": 12,
                                              "avgViewSeconds": 17.57,
                                              "ageDistribution": {
                                                "children": 5,
                                                "teenagers": 8,
                                                "youngAdults": 20,
                                                "midAged": 18,
                                                "seniors": 6
                                              },
                                              "genderDistribution": {
                                                "male": 30,
                                                "female": 27
                                              },
                                              "emotionDistribution": {
                                                "neutral": 25,
                                                "serious": 15,
                                                "happy": 12,
                                                "surprised": 5
                                              },
                                              "advertisements": [
                                                {
                                                  "adName": "Product A",
                                                  "totalViewers": 15,
                                                  "lookYes": 12,
                                                  "lookNo": 3
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getDashboardOverview() {
        log.debug("REST request to GET dashboard overview");

        DashboardOverviewResponse response = getDashboardOverviewUseCase.getDashboardOverview();

        log.debug("Returning dashboard overview with {} ads", response.getTotalAds());

        return ResponseEntity.ok(response);
    }
}

