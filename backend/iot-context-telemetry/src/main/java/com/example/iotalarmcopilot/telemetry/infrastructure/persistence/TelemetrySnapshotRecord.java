package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import com.example.iotalarmcopilot.telemetry.domain.DeviceId;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshot;
import com.example.iotalarmcopilot.telemetry.infrastructure.codec.TelemetryMetricsJsonCodec;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 遥测快照数据库实体
 */
@Data
public class TelemetrySnapshotRecord {

    private String deviceId;
    private Long lastTelemetryEventId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String metricsJson;
    private Instant lastReportedAt;
    private String lastRawJson;

    public static TelemetrySnapshotRecord fromDomain(TelemetrySnapshot snapshot) {
        TelemetrySnapshotRecord record = new TelemetrySnapshotRecord();
        record.setDeviceId(snapshot.deviceId().value());
        record.setLastTelemetryEventId(snapshot.lastTelemetryEventId());
        record.setTemperature(snapshot.temperature());
        record.setHumidity(snapshot.humidity());
        record.setMetricsJson(TelemetryMetricsJsonCodec.encode(snapshot.metrics()));
        record.setLastReportedAt(snapshot.lastReportedAt());
        record.setLastRawJson(snapshot.lastRawJson());
        return record;
    }

    public TelemetrySnapshot toDomain() {
        return new TelemetrySnapshot(
                new DeviceId(deviceId),
                lastTelemetryEventId,
                TelemetryMetricsJsonCodec.decode(metricsJson),
                lastReportedAt,
                lastRawJson);
    }
}
