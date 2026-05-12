package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.shared.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record TelemetryRecordedEvent(
        Long telemetryEventId,
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        Instant reportedAt) implements DomainEvent {

    public static final String EVENT_TYPE = "telemetry.recorded";

    public TelemetryRecordedEvent {
        Objects.requireNonNull(telemetryEventId, "telemetryEventId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(reportedAt, "reportedAt must not be null");
        if (deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId must not be blank");
        }
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant occurredAt() {
        return reportedAt;
    }
}
