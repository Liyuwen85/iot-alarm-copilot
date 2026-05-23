package com.example.iotalarmcopilot.inspection.application;

import java.time.Instant;

public record InspectionTicketVO(
        Long id,
        Long alarmId,
        String alarmDedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        String summary,
        String suggestion,
        String status,
        Instant alarmTriggeredAt,
        Instant createdAt,
        Instant confirmedAt,
        Instant closedAt) {
}
