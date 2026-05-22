package com.example.iotalarmcopilot.alarm.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.alarm.domain.policy.AlarmDedupKeyPolicy;
import com.example.iotalarmcopilot.alarm.domain.policy.AlarmSeverityPolicy;
import com.example.iotalarmcopilot.alarm.domain.policy.AlarmStatusPolicy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 告警实体
 *
 * @param id
 * @param dedupKey         防重键
 * @param ruleCode
 * @param telemetryEventId
 * @param deviceId
 * @param metricName
 * @param metricValue
 * @param threshold
 * @param severity
 * @param status
 * @param triggeredAt
 * @param acknowledgedAt
 * @param closedAt
 */
public record Alarm(
        Long id,
        AlarmDedupKey dedupKey,
        String ruleCode,
        Long telemetryEventId,
        String deviceId,
        String metricName,
        BigDecimal metricValue,
        BigDecimal threshold,
        AlarmSeverity severity,
        AlarmStatus status,
        Instant triggeredAt,
        Instant acknowledgedAt,
        Instant closedAt) {

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
     * @return
     */
    public static Alarm openFromRule(
            String ruleCode,
            Long telemetryEventId,
            String deviceId,
            String metricName,
            BigDecimal metricValue,
            BigDecimal threshold,
            Instant triggeredAt) {
        return new Alarm(
                null,
                AlarmDedupKeyPolicy.build(ruleCode, deviceId, telemetryEventId),
                ruleCode,
                telemetryEventId,
                deviceId,
                metricName,
                metricValue,
                threshold,
                AlarmSeverityPolicy.resolve(ruleCode),
                AlarmStatus.OPEN,
                triggeredAt,
                null,
                null);
    }

    /**
     * 确认告警
     *
     * @param acknowledgedAt
     * @return
     */
    public Alarm acknowledge(Instant acknowledgedAt) {
        Objects.requireNonNull(acknowledgedAt, "acknowledgedAt must not be null");
        AlarmStatusPolicy.ensureAcknowledgeAllowed(status);
        return new Alarm(
                id,
                dedupKey,
                ruleCode,
                telemetryEventId,
                deviceId,
                metricName,
                metricValue,
                threshold,
                severity,
                AlarmStatus.ACKED,
                triggeredAt,
                acknowledgedAt,
                closedAt);
    }

    /**
     * 关闭告警
     *
     * @param closedAt
     * @return
     */
    public Alarm close(Instant closedAt) {
        Objects.requireNonNull(closedAt, "closedAt must not be null");
        AlarmStatusPolicy.ensureCloseAllowed(status);
        return new Alarm(
                id,
                dedupKey,
                ruleCode,
                telemetryEventId,
                deviceId,
                metricName,
                metricValue,
                threshold,
                severity,
                AlarmStatus.CLOSED,
                triggeredAt,
                acknowledgedAt,
                closedAt);
    }

    public Alarm {
        Objects.requireNonNull(dedupKey, "dedupKey must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(telemetryEventId, "telemetryEventId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(metricName, "metricName must not be null");
        Objects.requireNonNull(metricValue, "metricValue must not be null");
        Objects.requireNonNull(threshold, "threshold must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(status, "status must not be null");
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
        AlarmStatusPolicy.validateLifecycle(status, triggeredAt, acknowledgedAt, closedAt);
    }
}
