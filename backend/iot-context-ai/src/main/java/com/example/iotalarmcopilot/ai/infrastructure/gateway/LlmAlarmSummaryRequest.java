package com.example.iotalarmcopilot.ai.infrastructure.gateway;

import java.time.Instant;
import java.util.Objects;

/**
 * 请求
 */
public record LlmAlarmSummaryRequest(
        Long alarmId,
        String alarmDedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        Instant alarmTriggeredAt,
        String promptVersion,
        String prompt) {

    public LlmAlarmSummaryRequest {
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(alarmDedupKey, "alarmDedupKey must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(alarmTriggeredAt, "alarmTriggeredAt must not be null");
        Objects.requireNonNull(promptVersion, "promptVersion must not be null");
        Objects.requireNonNull(prompt, "prompt must not be null");
    }
}
