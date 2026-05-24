package com.example.iotalarmcopilot.ai.application;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * AI告警摘要视图对象
 */
public record AiAlarmSummaryVO(
        Long id,
        Long alarmId,
        String alarmDedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        String status,
        Integer attemptCount,
        String summary,
        String possibleCause,
        String inspectionSuggestion,
        String riskLevel,
        BigDecimal confidence,
        String modelName,
        String promptVersion,
        String errorCode,
        String errorMessage,
        Instant alarmTriggeredAt,
        Instant createdAt,
        Instant updatedAt,
        Instant startedAt,
        Instant finishedAt) {
}
