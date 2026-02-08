package io.jeecloud.aidigitalsignage.analyticsetl.infrastructure.sqlite;

import io.jeecloud.aidigitalsignage.analyticsetl.domain.AdAnalytics;
import io.jeecloud.aidigitalsignage.analyticsetl.domain.AnalyticsRepository;
import io.jeecloud.aidigitalsignage.analyticsetl.domain.DashboardAnalytics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

/**
 * SQLite Repository Adapter (Infrastructure Layer)
 * 
 * Implements the AnalyticsRepository port to load data into SQLite.
 * Uses the SAME schema as digital-signage-service.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SqliteAnalyticsRepository implements AnalyticsRepository {
    
    private final DataSource sqliteDataSource;
    
    /**
     * Clear all analytics tables
     * 
     * Delete all rows to prepare for fresh ETL data.
     */
    @Override
    @Transactional
    public void clearAllAnalytics() {
        log.debug("Clearing all analytics tables in SQLite");
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDataSource);
        
        jdbcTemplate.update("DELETE FROM metrics_kpi");
        jdbcTemplate.update("DELETE FROM age_distribution");
        jdbcTemplate.update("DELETE FROM gender_distribution");
        jdbcTemplate.update("DELETE FROM emotion_distribution");
        jdbcTemplate.update("DELETE FROM advertisement");
        
        log.debug("All analytics tables cleared");
    }
    
    /**
     * Save dashboard analytics to SQLite
     * 
     * Insert into 4 tables:
     * - metrics_kpi
     * - age_distribution
     * - gender_distribution
     * - emotion_distribution
     */
    @Override
    @Transactional
    public void saveDashboardAnalytics(DashboardAnalytics analytics) {
        log.debug("Saving dashboard analytics to SQLite");
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDataSource);
        
        // Insert KPI metrics
        String kpiSql = """
                INSERT INTO metrics_kpi (total_audience, total_views, total_ads, avg_view_seconds)
                VALUES (?, ?, ?, ?)
                """;
        jdbcTemplate.update(kpiSql,
                analytics.getTotalAudience(),
                analytics.getTotalViews(),
                analytics.getTotalAds(),
                analytics.getAvgViewSeconds());
        
        // Insert age distribution
        String ageSql = """
                INSERT INTO age_distribution (children, teenagers, young_adults, mid_aged, seniors)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(ageSql,
                analytics.getChildren(),
                analytics.getTeenagers(),
                analytics.getYoungAdults(),
                analytics.getMidAged(),
                analytics.getSeniors());
        
        // Insert gender distribution
        String genderSql = """
                INSERT INTO gender_distribution (male, female)
                VALUES (?, ?)
                """;
        jdbcTemplate.update(genderSql,
                analytics.getMale(),
                analytics.getFemale());
        
        // Insert emotion distribution
        String emotionSql = """
                INSERT INTO emotion_distribution (neutral, serious, happy, surprised)
                VALUES (?, ?, ?, ?)
                """;
        jdbcTemplate.update(emotionSql,
                analytics.getNeutral(),
                analytics.getSerious(),
                analytics.getHappy(),
                analytics.getSurprised());
        
        log.debug("Dashboard analytics saved successfully");
    }
    
    /**
     * Save advertisement analytics to SQLite
     * 
     * Insert into advertisement table.
     */
    @Override
    @Transactional
    public void saveAdAnalytics(List<AdAnalytics> adAnalyticsList) {
        log.debug("Saving {} advertisement analytics to SQLite", adAnalyticsList.size());
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDataSource);
        
        String sql = """
                INSERT INTO advertisement (ad_name, total_viewers, look_yes, look_no)
                VALUES (?, ?, ?, ?)
                """;
        
        for (AdAnalytics ad : adAnalyticsList) {
            jdbcTemplate.update(sql,
                    ad.getAdName(),
                    ad.getTotalViewers(),
                    ad.getLookYes(),
                    ad.getLookNo());
        }
        
        log.debug("Advertisement analytics saved successfully");
    }
}
