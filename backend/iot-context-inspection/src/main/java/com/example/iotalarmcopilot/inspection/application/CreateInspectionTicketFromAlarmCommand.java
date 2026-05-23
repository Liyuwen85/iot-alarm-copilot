package com.example.iotalarmcopilot.inspection.application;

import java.time.Instant;

public record CreateInspectionTicketFromAlarmCommand(
        Long alarmId,
        String alarmDedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        Instant alarmTriggeredAt,
        Instant createdAt) {
}
