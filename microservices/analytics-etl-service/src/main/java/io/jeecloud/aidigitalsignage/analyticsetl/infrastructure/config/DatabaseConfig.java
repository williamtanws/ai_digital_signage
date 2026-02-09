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
 * Configures TDengine data source for extracting gaze events.
 * Analytics are sent to digital-signage-service via REST API (not direct DB access).
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
    
    /**
     * TDengine DataSource (Source for ETL)
     * 
     * TDengine uses JDBC driver: com.taosdata.jdbc.TSDBDriver
     * Default port: 6041 (REST API)
     */
    @Bean(name = "tdengineDataSource")
    @Primary
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
}
