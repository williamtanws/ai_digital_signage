package io.jeecloud.aidigitalsignage.digitalsignage.application.port.in;

import io.jeecloud.aidigitalsignage.digitalsignage.application.dto.UpdateAnalyticsRequest;

/**
 * Update Analytics Use Case (Command Side - CQRS)
 * 
 * Input port for updating dashboard analytics from ETL service.
 * Implemented by command service in application layer.
 */
public interface UpdateAnalyticsUseCase {

    /**
     * Update analytics data (replace all existing data)
     * 
     * @param request Analytics data from ETL service
     */
    void updateAnalytics(UpdateAnalyticsRequest request);
}
