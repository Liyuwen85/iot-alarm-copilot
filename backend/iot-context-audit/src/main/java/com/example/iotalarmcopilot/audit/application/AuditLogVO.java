package com.example.iotalarmcopilot.audit.application;

import java.time.Instant;

public record AuditLogVO(
        Long id,
        String eventType,
        String aggregateType,
        String aggregateId,
        String deviceId,
        String payloadJson,
        Instant occurredAt,
        Instant createdAt) {
}
