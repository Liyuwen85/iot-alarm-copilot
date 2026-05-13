package com.example.iotalarmcopilot.rule.application;

import com.example.iotalarmcopilot.shared.BaseDomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 评估规则命令
 * @param telemetryEventId
 * @param deviceId
 * @param temperature
 * @param humidity
 * @param reportedAt
 */
public record EvaluateTelemetryRuleCommand(
        Long telemetryEventId,
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        Instant reportedAt) {

    public EvaluateTelemetryRuleCommand {
        Objects.requireNonNull(telemetryEventId, "telemetryEventId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(reportedAt, "reportedAt must not be null");
        if (deviceId.isBlank()) {
            throw new BaseDomainException("deviceId must not be blank");
        }
        if (temperature == null && humidity == null) {
            throw new BaseDomainException("At least one telemetry metric is required for rule evaluation");
        }
    }
}
