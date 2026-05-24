package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * AI告警摘要生成失败事件
 */
public record AlarmAiSummaryFailedEvent(
        Long summaryTaskId,
        Long alarmId,
        String dedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        String errorCode,
        String errorMessage,
        Instant failedAt) implements DomainEvent {

    public static final String EVENT_TYPE = "alarm.ai.summary.failed";

    public AlarmAiSummaryFailedEvent {
        Objects.requireNonNull(summaryTaskId, "summaryTaskId must not be null");
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(dedupKey, "dedupKey must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(errorCode, "errorCode must not be null");
        Objects.requireNonNull(errorMessage, "errorMessage must not be null");
        Objects.requireNonNull(failedAt, "failedAt must not be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant occurredAt() {
        return failedAt;
    }
}
