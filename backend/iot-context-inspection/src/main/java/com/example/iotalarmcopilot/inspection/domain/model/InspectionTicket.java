package com.example.iotalarmcopilot.inspection.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.inspection.domain.policy.InspectionAdvicePolicy;
import com.example.iotalarmcopilot.inspection.domain.policy.InspectionStatusPolicy;

import java.time.Instant;
import java.util.Objects;

/**
 * 巡检工单
 */
public record InspectionTicket(
        Long id,
        Long alarmId,
        String alarmDedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        String summary,
        String suggestion,
        InspectionStatus status,
        Instant alarmTriggeredAt,
        Instant createdAt,
        Instant confirmedAt,
        Instant closedAt) {

    public static InspectionTicket openFromAlarm(
            Long alarmId,
            String alarmDedupKey,
            String ruleCode,
            String deviceId,
            String severity,
            Instant alarmTriggeredAt,
            Instant createdAt) {
        return new InspectionTicket(
                null,
                alarmId,
                alarmDedupKey,
                ruleCode,
                deviceId,
                severity,
                InspectionAdvicePolicy.buildSummary(ruleCode, deviceId),
                InspectionAdvicePolicy.buildSuggestion(severity, ruleCode),
                InspectionStatus.PENDING,
                alarmTriggeredAt,
                createdAt,
                null,
                null);
    }

    public InspectionTicket confirm(Instant confirmedAt) {
        Objects.requireNonNull(confirmedAt, "confirmedAt must not be null");
        InspectionStatusPolicy.ensureConfirmAllowed(status);
        return new InspectionTicket(
                id,
                alarmId,
                alarmDedupKey,
                ruleCode,
                deviceId,
                severity,
                summary,
                suggestion,
                InspectionStatus.CONFIRMED,
                alarmTriggeredAt,
                createdAt,
                confirmedAt,
                null);
    }

    public InspectionTicket close(Instant closedAt) {
        Objects.requireNonNull(closedAt, "closedAt must not be null");
        InspectionStatusPolicy.ensureCloseAllowed(status);
        return new InspectionTicket(
                id,
                alarmId,
                alarmDedupKey,
                ruleCode,
                deviceId,
                severity,
                summary,
                suggestion,
                InspectionStatus.CLOSED,
                alarmTriggeredAt,
                createdAt,
                confirmedAt,
                closedAt);
    }

    public InspectionTicket {
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(alarmDedupKey, "alarmDedupKey must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(suggestion, "suggestion must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(alarmTriggeredAt, "alarmTriggeredAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        if (alarmDedupKey.isBlank()) {
            throw new BaseDomainException("alarmDedupKey must not be blank");
        }
        if (ruleCode.isBlank()) {
            throw new BaseDomainException("ruleCode must not be blank");
        }
        if (deviceId.isBlank()) {
            throw new BaseDomainException("deviceId must not be blank");
        }
        if (severity.isBlank()) {
            throw new BaseDomainException("severity must not be blank");
        }
        if (summary.isBlank()) {
            throw new BaseDomainException("summary must not be blank");
        }
        if (suggestion.isBlank()) {
            throw new BaseDomainException("suggestion must not be blank");
        }
        InspectionStatusPolicy.validateLifecycle(status, createdAt, confirmedAt, closedAt);
    }
}
