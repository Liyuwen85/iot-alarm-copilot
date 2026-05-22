package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import com.example.iotalarmcopilot.telemetry.domain.DeviceId;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.infrastructure.codec.TelemetryMetricsJsonCodec;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 遥测事件记录数据库实体
 */
@Data
public class TelemetryEventRecord {

    private Long id;
    private String deviceId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String metricsJson;
    private Instant reportedAt;
    private String rawJson;

    public static TelemetryEventRecord fromDomain(TelemetryEvent event) {
        TelemetryEventRecord record = new TelemetryEventRecord();
        record.setId(event.id());
        record.setDeviceId(event.deviceId().value());
        record.setTemperature(event.temperature());
        record.setHumidity(event.humidity());
        record.setMetricsJson(TelemetryMetricsJsonCodec.encode(event.metrics()));
        record.setReportedAt(event.reportedAt());
        record.setRawJson(event.rawJson());
        return record;
    }

    public TelemetryEvent toDomain() {
        return new TelemetryEvent(
                id,
                new DeviceId(deviceId),
                TelemetryMetricsJsonCodec.decode(metricsJson),
                reportedAt,
                rawJson);
    }
}
