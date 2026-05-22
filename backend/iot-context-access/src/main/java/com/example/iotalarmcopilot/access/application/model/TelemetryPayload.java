package com.example.iotalarmcopilot.access.application.model;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * 接入层标准化后的遥测载荷
 *
 * @param deviceId
 * @param metrics
 * @param reportedAt
 * @param rawJson
 */
public record TelemetryPayload(
        String deviceId,
        TelemetryMetrics metrics,
        Instant reportedAt,
        String rawJson) {

    public TelemetryPayload {
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(metrics, "metrics must not be null");
        Objects.requireNonNull(reportedAt, "reportedAt must not be null");
        Objects.requireNonNull(rawJson, "rawJson must not be null");
        if (deviceId.isBlank()) {
            throw new BaseDomainException("deviceId must not be blank");
        }
        if (rawJson.isBlank()) {
            throw new BaseDomainException("rawJson must not be blank");
        }
    }

    public java.math.BigDecimal temperature() {
        return metrics.temperature();
    }

    public java.math.BigDecimal humidity() {
        return metrics.humidity();
    }
}
