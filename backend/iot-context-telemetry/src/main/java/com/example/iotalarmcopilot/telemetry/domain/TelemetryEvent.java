package com.example.iotalarmcopilot.telemetry.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 遥测领域实体
 */
public record TelemetryEvent(
        Long id,
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        Instant reportedAt,
        String rawJson) {

    public TelemetryEvent {
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(reportedAt, "reportedAt must not be null");
        Objects.requireNonNull(rawJson, "rawJson must not be null");
    }
}
