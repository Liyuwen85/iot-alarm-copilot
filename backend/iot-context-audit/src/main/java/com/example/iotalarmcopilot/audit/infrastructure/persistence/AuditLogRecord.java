package com.example.iotalarmcopilot.audit.infrastructure.persistence;

import com.example.iotalarmcopilot.audit.domain.AuditAggregateId;
import com.example.iotalarmcopilot.audit.domain.AuditAggregateType;
import com.example.iotalarmcopilot.audit.domain.AuditEventType;
import com.example.iotalarmcopilot.audit.domain.AuditLogEntry;
import lombok.Data;

import java.time.Instant;

@Data
public class AuditLogRecord {

    private Long id;
    private String eventType;
    private String aggregateType;
    private String aggregateId;
    private String deviceId;
    private String payloadJson;
    private Instant occurredAt;
    private Instant createdAt;

    public static AuditLogRecord fromDomain(AuditLogEntry entry) {
        AuditLogRecord record = new AuditLogRecord();
        record.setId(entry.id());
        record.setEventType(entry.eventType().value());
        record.setAggregateType(entry.aggregateType().value());
        record.setAggregateId(entry.aggregateId().value());
        record.setDeviceId(entry.deviceId());
        record.setPayloadJson(entry.payloadJson());
        record.setOccurredAt(entry.occurredAt());
        return record;
    }

    public AuditLogEntry toDomain() {
        return new AuditLogEntry(
                id,
                new AuditEventType(eventType),
                new AuditAggregateType(aggregateType),
                new AuditAggregateId(aggregateId),
                deviceId,
                payloadJson,
                occurredAt);
    }
}
