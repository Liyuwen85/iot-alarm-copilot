package com.example.iotalarmcopilot.telemetry.infrastructure.timeseries;

import com.example.iotalarmcopilot.telemetry.application.TelemetryEventVO;
import com.example.iotalarmcopilot.telemetry.application.TelemetryHotDataPort;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * TimescaleDB 热点数据实现
 */
@Component
@ConditionalOnProperty(prefix = "iot.timeseries", name = "enabled", havingValue = "true")
public class TimescaleTelemetryHotDataPort implements TelemetryHotDataPort {

    private final JdbcTemplate jdbcTemplate;

    public TimescaleTelemetryHotDataPort(
            @Qualifier("telemetryTimeseriesJdbcTemplate") JdbcTemplate telemetryTimeseriesJdbcTemplate) {
        this.jdbcTemplate = telemetryTimeseriesJdbcTemplate;
    }

    @Override
    public void append(TelemetryEvent event) {
        TelemetryPointRecord record = TelemetryPointRecord.fromDomain(event);
        jdbcTemplate.update("""
                        INSERT INTO telemetry_point (
                            telemetry_event_id,
                            device_id,
                            temperature,
                            humidity,
                            metrics_json,
                            reported_at,
                            raw_json
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (telemetry_event_id, reported_at) DO NOTHING
                        """,
                record.getTelemetryEventId(),
                record.getDeviceId(),
                record.getTemperature(),
                record.getHumidity(),
                record.getMetricsJson(),
                Timestamp.from(record.getReportedAt()),
                record.getRawJson());
    }

    @Override
    public List<TelemetryEventVO> recent(int limit) {
        return jdbcTemplate.query("""
                        SELECT
                            telemetry_event_id,
                            device_id,
                            temperature,
                            humidity,
                            metrics_json,
                            reported_at,
                            raw_json
                        FROM telemetry_point
                        ORDER BY reported_at DESC, telemetry_event_id DESC
                        LIMIT ?
                        """,
                (resultSet, rowNum) -> mapRow(resultSet).toVO(),
                limit);
    }

    private TelemetryPointRecord mapRow(ResultSet resultSet) throws SQLException {
        TelemetryPointRecord record = new TelemetryPointRecord();
        record.setTelemetryEventId(resultSet.getLong("telemetry_event_id"));
        record.setDeviceId(resultSet.getString("device_id"));
        record.setTemperature(resultSet.getBigDecimal("temperature"));
        record.setHumidity(resultSet.getBigDecimal("humidity"));
        record.setMetricsJson(resultSet.getString("metrics_json"));
        Timestamp reportedAt = resultSet.getTimestamp("reported_at");
        record.setReportedAt(reportedAt == null ? null : reportedAt.toInstant());
        record.setRawJson(resultSet.getString("raw_json"));
        return record;
    }
}
