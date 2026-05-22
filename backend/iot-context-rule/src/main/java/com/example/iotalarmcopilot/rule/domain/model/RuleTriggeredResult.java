package com.example.iotalarmcopilot.rule.domain.model;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 规则触发-领域结果模型
 *
 * @param ruleCode
 * @param telemetryEventId
 * @param deviceId
 * @param metricName
 * @param metricValue
 * @param threshold
 * @param triggeredAt
 */
public record RuleTriggeredResult(
        RuleCode ruleCode,
        Long telemetryEventId,
        DeviceId deviceId,
        TelemetryMetricName metricName,
        BigDecimal metricValue,
        BigDecimal threshold,
        Instant triggeredAt) {

    public RuleTriggeredResult {
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(telemetryEventId, "telemetryEventId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(metricName, "metricName must not be null");
        Objects.requireNonNull(metricValue, "metricValue must not be null");
        Objects.requireNonNull(threshold, "threshold must not be null");
        Objects.requireNonNull(triggeredAt, "triggeredAt must not be null");
    }
}
