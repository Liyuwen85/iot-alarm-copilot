package com.example.iotalarmcopilot.alarm.application;

import com.example.iotalarmcopilot.BaseDomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 从规则创建告警
 *
 * @param ruleCode
 * @param telemetryEventId
 * @param deviceId
 * @param metricName
 * @param metricValue
 * @param threshold
 * @param triggeredAt
 */
public record CreateAlarmFromRuleCommand(
        String ruleCode,
        Long telemetryEventId,
        String deviceId,
        String metricName,
        BigDecimal metricValue,
        BigDecimal threshold,
        Instant triggeredAt) {

    public CreateAlarmFromRuleCommand {
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(telemetryEventId, "telemetryEventId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(metricName, "metricName must not be null");
        Objects.requireNonNull(metricValue, "metricValue must not be null");
        Objects.requireNonNull(threshold, "threshold must not be null");
        Objects.requireNonNull(triggeredAt, "triggeredAt must not be null");
        if (ruleCode.isBlank()) {
            throw new BaseDomainException("ruleCode must not be blank");
        }
        if (deviceId.isBlank()) {
            throw new BaseDomainException("deviceId must not be blank");
        }
        if (metricName.isBlank()) {
            throw new BaseDomainException("metricName must not be blank");
        }
    }
}
