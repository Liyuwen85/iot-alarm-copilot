package com.example.iotalarmcopilot.ai.application;

import java.time.Instant;
import java.util.Objects;

public record CreateAiSummaryTaskCommand(
        Long alarmId,
        String alarmDedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        Instant alarmTriggeredAt,
        Instant createdAt) {

    public CreateAiSummaryTaskCommand {
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(alarmDedupKey, "alarmDedupKey must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(alarmTriggeredAt, "alarmTriggeredAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
