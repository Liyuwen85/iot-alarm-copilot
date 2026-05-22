package com.example.iotalarmcopilot.telemetry.domain;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.BaseDomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 遥测事件实体
 */
public record TelemetryEvent(
        Long id,
        DeviceId deviceId,
        TelemetryMetrics metrics,
        Instant reportedAt,
        String rawJson) {

    public static TelemetryEvent record(
            Long id,
            String deviceId,
            TelemetryMetrics metrics,
            Instant reportedAt,
            String rawJson) {
        return new TelemetryEvent(
                id,
                new DeviceId(deviceId),
                metrics,
                reportedAt,
                rawJson);
    }

    public static TelemetryEvent record(
            String deviceId,
            TelemetryMetrics metrics,
            Instant reportedAt,
            String rawJson) {
        return record(null, deviceId, metrics, reportedAt, rawJson);
    }

    public static TelemetryEvent record(
            String deviceId,
            BigDecimal temperature,
            BigDecimal humidity,
            Instant reportedAt,
            String rawJson) {
        return record(
                deviceId,
                TelemetryMetrics.ofTemperatureAndHumidity(temperature, humidity),
                reportedAt,
                rawJson);
    }

    public TelemetryEvent {
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(metrics, "metrics must not be null");
        Objects.requireNonNull(reportedAt, "reportedAt must not be null");
        Objects.requireNonNull(rawJson, "rawJson must not be null");
        if (rawJson.isBlank()) {
            throw new BaseDomainException("rawJson must not be blank");
        }
    }

    public BigDecimal temperature() {
        return metrics.temperature();
    }

    public BigDecimal humidity() {
        return metrics.humidity();
    }
}
