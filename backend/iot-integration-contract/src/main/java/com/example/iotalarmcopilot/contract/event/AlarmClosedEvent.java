package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * 告警关闭事件
 */
public record AlarmClosedEvent(
        Long alarmId,
        String dedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        Instant closedAt) implements DomainEvent {

    public static final String EVENT_TYPE = "alarm.closed";

    public AlarmClosedEvent {
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(dedupKey, "dedupKey must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(closedAt, "closedAt must not be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant occurredAt() {
        return closedAt;
    }
}
