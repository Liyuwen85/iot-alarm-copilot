package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * 告警关闭事件
 */
public record AlarmAcknowledgedEvent(
        Long alarmId,
        String dedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        Instant acknowledgedAt) implements DomainEvent {

    public static final String EVENT_TYPE = "alarm.acknowledged";

    public AlarmAcknowledgedEvent {
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(dedupKey, "dedupKey must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(acknowledgedAt, "acknowledgedAt must not be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant occurredAt() {
        return acknowledgedAt;
    }
}
