package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * 巡检工单关闭事件
 */
public record InspectionTicketClosedEvent(
        Long inspectionTicketId,
        Long alarmId,
        String alarmDedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        Instant closedAt) implements DomainEvent {

    public static final String EVENT_TYPE = "inspection.ticket.closed";

    public InspectionTicketClosedEvent {
        Objects.requireNonNull(inspectionTicketId, "inspectionTicketId must not be null");
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(alarmDedupKey, "alarmDedupKey must not be null");
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
