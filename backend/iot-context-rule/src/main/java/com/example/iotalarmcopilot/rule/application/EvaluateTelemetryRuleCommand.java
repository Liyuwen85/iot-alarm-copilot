package com.example.iotalarmcopilot.rule.application;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * 评估规则命令
 *
 * @param telemetryEventId
 * @param deviceId
 * @param metrics
 * @param reportedAt
 */
public record EvaluateTelemetryRuleCommand(
        Long telemetryEventId,
        String deviceId,
        TelemetryMetrics metrics,
        Instant reportedAt) {

    public EvaluateTelemetryRuleCommand {
        Objects.requireNonNull(telemetryEventId, "telemetryEventId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(metrics, "metrics must not be null");
        Objects.requireNonNull(reportedAt, "reportedAt must not be null");
        if (deviceId.isBlank()) {
            throw new BaseDomainException("deviceId must not be blank");
        }
    }

    public java.math.BigDecimal temperature() {
        return metrics.temperature();
    }

    public java.math.BigDecimal humidity() {
        return metrics.humidity();
    }
}
