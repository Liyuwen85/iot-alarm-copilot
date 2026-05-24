package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * AI告警摘要生成成功事件
 */
public record AlarmAiSummaryGeneratedEvent(
        Long summaryTaskId,
        Long alarmId,
        String dedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        String riskLevel,
        BigDecimal confidence,
        String modelName,
        String promptVersion,
        Instant generatedAt) implements DomainEvent {

    public static final String EVENT_TYPE = "alarm.ai.summary.generated";

    public AlarmAiSummaryGeneratedEvent {
        Objects.requireNonNull(summaryTaskId, "summaryTaskId must not be null");
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(dedupKey, "dedupKey must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(riskLevel, "riskLevel must not be null");
        Objects.requireNonNull(confidence, "confidence must not be null");
        Objects.requireNonNull(modelName, "modelName must not be null");
        Objects.requireNonNull(promptVersion, "promptVersion must not be null");
        Objects.requireNonNull(generatedAt, "generatedAt must not be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant occurredAt() {
        return generatedAt;
    }
}
