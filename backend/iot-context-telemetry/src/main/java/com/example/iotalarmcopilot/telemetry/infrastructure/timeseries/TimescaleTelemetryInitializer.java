package com.example.iotalarmcopilot.telemetry.infrastructure.timeseries;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * TimescaleDB 表初始化
 */
public class TimescaleTelemetryInitializer {

    private final JdbcTemplate jdbcTemplate;

    public TimescaleTelemetryInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initializeSchema();
    }

    private void initializeSchema() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS timescaledb");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS telemetry_point (
                    telemetry_event_id BIGINT NOT NULL,
                    device_id VARCHAR(64) NOT NULL,
                    temperature NUMERIC(10, 2) NULL,
                    humidity NUMERIC(10, 2) NULL,
                    reported_at TIMESTAMPTZ NOT NULL,
                    raw_json TEXT NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    UNIQUE (telemetry_event_id, reported_at)
                )
                """);
        jdbcTemplate.queryForObject(
                "SELECT create_hypertable('telemetry_point', 'reported_at', if_not_exists => TRUE)",
                String.class);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_telemetry_point_device_reported_at
                ON telemetry_point (device_id, reported_at DESC)
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_telemetry_point_reported_at
                ON telemetry_point (reported_at DESC)
                """);
    }
}
