package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * 创建巡检工单事件
 */
public record InspectionTicketCreatedEvent(
        Long inspectionTicketId,
        Long alarmId,
        String alarmDedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        Instant createdAt) implements DomainEvent {

    public static final String EVENT_TYPE = "inspection.ticket.created";

    public InspectionTicketCreatedEvent {
        Objects.requireNonNull(inspectionTicketId, "inspectionTicketId must not be null");
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(alarmDedupKey, "alarmDedupKey must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant occurredAt() {
        return createdAt;
    }
}
