package com.example.iotalarmcopilot.audit.domain;

import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * 审计日志实体
 *
 * @param id
 * @param eventType
 * @param aggregateType
 * @param aggregateId
 * @param deviceId
 * @param payloadJson
 * @param occurredAt
 */
public record AuditLogEntry(
        Long id,
        AuditEventType eventType,
        AuditAggregateType aggregateType,
        AuditAggregateId aggregateId,
        String deviceId,
        String payloadJson,
        Instant occurredAt) {

    public static AuditLogEntry record(
            String eventType,
            String aggregateType,
            String aggregateId,
            String deviceId,
            String payloadJson,
            Instant occurredAt) {
        return new AuditLogEntry(
                null,
                new AuditEventType(eventType),
                new AuditAggregateType(aggregateType),
                new AuditAggregateId(aggregateId),
                deviceId,
                payloadJson,
                occurredAt);
    }

    public AuditLogEntry {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        Objects.requireNonNull(payloadJson, "payloadJson must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        if (payloadJson.isBlank()) {
            throw new BaseDomainException("payloadJson must not be blank");
        }
    }
}
