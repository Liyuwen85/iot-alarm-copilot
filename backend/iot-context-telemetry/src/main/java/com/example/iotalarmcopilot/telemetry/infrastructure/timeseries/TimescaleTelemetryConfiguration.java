package com.example.iotalarmcopilot.telemetry.infrastructure.timeseries;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * TimescaleDB配置，独立配置，避免和主数据源冲突（主数据源从MySQL切换到PostgreSQL仅仅是为了方便，少用一个实例）
 */
@Configuration
@ConditionalOnProperty(prefix = "iot.timeseries", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(TelemetryTimeseriesProperties.class)
public class TimescaleTelemetryConfiguration {

    @Bean(destroyMethod = "close")
    TimescaleTelemetryResources telemetryTimeseriesResources(TelemetryTimeseriesProperties properties) {
        // 创建数据源Hikari
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(properties.getDriverClassName());
        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setPoolName("telemetry-timeseries-pool");
        dataSource.setMaximumPoolSize(4);
        dataSource.setMinimumIdle(1);
        return new TimescaleTelemetryResources(dataSource);
    }

    @Bean
    JdbcTemplate telemetryTimeseriesJdbcTemplate(
            @Qualifier("telemetryTimeseriesResources") TimescaleTelemetryResources telemetryTimeseriesResources) {
        return telemetryTimeseriesResources.jdbcTemplate();
    }

    @Bean
    TimescaleTelemetryInitializer timescaleTelemetryInitializer(
            @Qualifier("telemetryTimeseriesJdbcTemplate") JdbcTemplate telemetryTimeseriesJdbcTemplate) {
        return new TimescaleTelemetryInitializer(telemetryTimeseriesJdbcTemplate);
    }

    /**
     * 自动注销数据源，避免内存泄漏
     */
    static final class TimescaleTelemetryResources implements AutoCloseable {

        private final HikariDataSource dataSource;
        private final JdbcTemplate jdbcTemplate;

        private TimescaleTelemetryResources(HikariDataSource dataSource) {
            this.dataSource = dataSource;
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }

        JdbcTemplate jdbcTemplate() {
            return jdbcTemplate;
        }

        @Override
        public void close() {
            dataSource.close();
        }
    }
}
