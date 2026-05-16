package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.telemetry.domain.DeviceId;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshot;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 快照记录持久化实体
 */
@Data
public class TelemetrySnapshotRecord {

    private String deviceId;
    private Long lastTelemetryEventId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private Instant lastReportedAt;
    private String lastRawJson;

    public static TelemetrySnapshotRecord fromDomain(TelemetrySnapshot snapshot) {
        TelemetrySnapshotRecord record = new TelemetrySnapshotRecord();
        record.setDeviceId(snapshot.deviceId().value());
        record.setLastTelemetryEventId(snapshot.lastTelemetryEventId());
        record.setTemperature(snapshot.temperature());
        record.setHumidity(snapshot.humidity());
        record.setLastReportedAt(snapshot.lastReportedAt());
        record.setLastRawJson(snapshot.lastRawJson());
        return record;
    }

    public TelemetrySnapshot toDomain() {
        return new TelemetrySnapshot(
                new DeviceId(deviceId),
                lastTelemetryEventId,
                TelemetryMetrics.ofTemperatureAndHumidity(temperature, humidity),
                lastReportedAt,
                lastRawJson);
    }
}
