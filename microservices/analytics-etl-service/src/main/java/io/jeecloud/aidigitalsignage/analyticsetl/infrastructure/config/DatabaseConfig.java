package io.jeecloud.aidigitalsignage.analyticsetl.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Database Configuration (Infrastructure Layer)
 * 
 * Configures two data sources:
 * 1. TDengine (source) - for extracting gaze events
 * 2. SQLite (target) - for loading analytics
 */
@Configuration
public class DatabaseConfig {
    
    @Value("${tdengine.url}")
    private String tdengineUrl;
    
    @Value("${tdengine.username}")
    private String tdengineUsername;
    
    @Value("${tdengine.password}")
    private String tdenginePassword;
    
    @Value("${tdengine.database}")
    private String tdengineDatabase;
    
    @Value("${sqlite.url}")
    private String sqliteUrl;
    
    /**
     * TDengine DataSource (Source for ETL)
     * 
     * TDengine uses JDBC driver: com.taosdata.jdbc.TSDBDriver
     * Default port: 6041 (REST API)
     */
    @Bean(name = "tdengineDataSource")
    public DataSource tdengineDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.taosdata.jdbc.rs.RestfulDriver");
        config.setJdbcUrl(tdengineUrl + "/" + tdengineDatabase);
        config.setUsername(tdengineUsername);
        config.setPassword(tdenginePassword);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(10000);
        
        return new HikariDataSource(config);
    }
    
    /**
     * SQLite DataSource (Target for ETL)
     * 
     * SQLite is the same database used by digital-signage-service.
     * This ensures we're populating the correct schema.
     */
    @Bean(name = "sqliteDataSource")
    @Primary
    public DataSource sqliteDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl(sqliteUrl);
        config.setMaximumPoolSize(1); // SQLite single-writer
        config.setConnectionTimeout(10000);
        
        return new HikariDataSource(config);
    }
}
