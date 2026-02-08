package io.jeecloud.aidigitalsignage.digitalsignage.application.port.in;

import io.jeecloud.aidigitalsignage.digitalsignage.application.dto.DashboardOverviewResponse;

/**
 * Get Dashboard Overview Use Case (Query Side - CQRS)
 * 
 * Input port for retrieving dashboard overview data.
 * Implemented by query service in application layer.
 */
public interface GetDashboardOverviewUseCase {

    /**
     * Retrieve complete dashboard overview with all metrics
     * 
     * @return Dashboard data with KPIs, demographics, and ad performance
     */
    DashboardOverviewResponse getDashboardOverview();
}
