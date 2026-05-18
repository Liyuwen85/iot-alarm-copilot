package com.example.iotalarmcopilot.audit.application;

import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * 审计记录命令
 */
public record RecordAuditLogCommand(
        String eventType,
        String aggregateType,
        String aggregateId,
        String deviceId,
        String payloadJson,
        Instant occurredAt) {

    public RecordAuditLogCommand {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        Objects.requireNonNull(payloadJson, "payloadJson must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        if (eventType.isBlank()) {
            throw new BaseDomainException("eventType must not be blank");
        }
        if (aggregateType.isBlank()) {
            throw new BaseDomainException("aggregateType must not be blank");
        }
        if (aggregateId.isBlank()) {
            throw new BaseDomainException("aggregateId must not be blank");
        }
        if (payloadJson.isBlank()) {
            throw new BaseDomainException("payloadJson must not be blank");
        }
    }
}
