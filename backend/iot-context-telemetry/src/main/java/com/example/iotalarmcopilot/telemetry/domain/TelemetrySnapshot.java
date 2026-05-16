package com.example.iotalarmcopilot.telemetry.domain;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 最近的设备遥测快照数据
 */
public record TelemetrySnapshot(
        DeviceId deviceId,
        Long lastTelemetryEventId,
        TelemetryMetrics metrics,
        Instant lastReportedAt,
        String lastRawJson) {

    /**
     * 创建一个快照
     * @param event
     * @return
     */
    public static TelemetrySnapshot capture(TelemetryEvent event) {
        return new TelemetrySnapshot(
                event.deviceId(),
                event.id(),
                event.metrics(),
                event.reportedAt(),
                event.rawJson());
    }

    /**
     * 更新快照
     * @param event
     * @return
     */
    public TelemetrySnapshot refreshBy(TelemetryEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        if (!deviceId.equals(event.deviceId())) {
            throw new BaseDomainException("Telemetry snapshot device mismatch");
        }
        if (isOlderThan(event)) {
            return this;
        }
        return new TelemetrySnapshot(
                deviceId,
                event.id(),
                mergeMetrics(event.metrics()),
                event.reportedAt(),
                event.rawJson());
    }

    public TelemetrySnapshot {
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(lastTelemetryEventId, "lastTelemetryEventId must not be null");
        Objects.requireNonNull(metrics, "metrics must not be null");
        Objects.requireNonNull(lastReportedAt, "lastReportedAt must not be null");
        Objects.requireNonNull(lastRawJson, "lastRawJson must not be null");
        if (lastRawJson.isBlank()) {
            throw new BaseDomainException("lastRawJson must not be blank");
        }
    }

    public BigDecimal temperature() {
        return metrics.temperature();
    }

    public BigDecimal humidity() {
        return metrics.humidity();
    }

    private boolean isOlderThan(TelemetryEvent event) {
        return event.reportedAt().isBefore(lastReportedAt)
                || (event.reportedAt().equals(lastReportedAt) && event.id() < lastTelemetryEventId);
    }

    private TelemetryMetrics mergeMetrics(TelemetryMetrics incoming) {
        Map<TelemetryMetricName, BigDecimal> merged = new LinkedHashMap<>(metrics.values());
        merged.putAll(incoming.values());
        return new TelemetryMetrics(merged);
    }
}
