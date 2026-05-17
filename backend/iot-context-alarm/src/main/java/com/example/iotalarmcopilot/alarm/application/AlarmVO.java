package com.example.iotalarmcopilot.alarm.application;

import java.math.BigDecimal;
import java.time.Instant;

public record AlarmVO(
        Long id,
        String dedupKey,
        String ruleCode,
        Long telemetryEventId,
        String deviceId,
        String metricName,
        BigDecimal metricValue,
        BigDecimal threshold,
        String severity,
        String status,
        Instant triggeredAt,
        Instant acknowledgedAt,
        Instant closedAt) {
}
