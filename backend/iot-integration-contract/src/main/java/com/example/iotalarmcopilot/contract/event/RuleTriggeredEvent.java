package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 触发规则事件
 *
 * @param ruleCode
 * @param telemetryEventId
 * @param deviceId
 * @param metricName
 * @param metricValue
 * @param threshold
 * @param triggeredAt
 */
public record RuleTriggeredEvent(
        String ruleCode,
        Long telemetryEventId,
        String deviceId,
        TelemetryMetricName metricName,
        BigDecimal metricValue,
        BigDecimal threshold,
        Instant triggeredAt) implements DomainEvent {

    public static final String EVENT_TYPE = "rule.triggered";

    public RuleTriggeredEvent {
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(telemetryEventId, "telemetryEventId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(metricName, "metricName must not be null");
        Objects.requireNonNull(metricValue, "metricValue must not be null");
        Objects.requireNonNull(threshold, "threshold must not be null");
        Objects.requireNonNull(triggeredAt, "triggeredAt must not be null");
        if (ruleCode.isBlank()) {
            throw new IllegalArgumentException("ruleCode must not be blank");
        }
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
        return triggeredAt;
    }
}
