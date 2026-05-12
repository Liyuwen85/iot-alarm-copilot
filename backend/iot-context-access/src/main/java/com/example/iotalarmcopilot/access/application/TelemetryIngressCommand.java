package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.shared.BaseDomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record TelemetryIngressCommand(
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        Instant reportedAt,
        String rawJson) {

    public TelemetryIngressCommand {
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(reportedAt, "reportedAt must not be null");
        Objects.requireNonNull(rawJson, "rawJson must not be null");
        if (deviceId.isBlank()) {
            throw new BaseDomainException("deviceId must not be blank");
        }
        if (rawJson.isBlank()) {
            throw new BaseDomainException("rawJson must not be blank");
        }
        if (temperature == null && humidity == null) {
            throw new BaseDomainException("Telemetry payload must include at least one metric");
        }
    }
}
