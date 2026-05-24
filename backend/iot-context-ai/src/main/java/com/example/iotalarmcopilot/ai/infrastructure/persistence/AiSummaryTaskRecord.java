package com.example.iotalarmcopilot.ai.infrastructure.persistence;

import com.example.iotalarmcopilot.ai.domain.model.AiSummaryStatus;
import com.example.iotalarmcopilot.ai.domain.model.AiSummaryTask;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * AI告警摘要任务记录
 */
@Data
public class AiSummaryTaskRecord {

    private Long id;
    private Long alarmId;
    private String alarmDedupKey;
    private String ruleCode;
    private String deviceId;
    private String severity;
    private String status;
    private Integer attemptCount;
    private String summary;
    private String possibleCause;
    private String inspectionSuggestion;
    private String riskLevel;
    private BigDecimal confidence;
    private String modelName;
    private String promptVersion;
    private String requestPayload;
    private String responsePayload;
    private String errorCode;
    private String errorMessage;
    private Instant alarmTriggeredAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant startedAt;
    private Instant finishedAt;

    public static AiSummaryTaskRecord fromDomain(AiSummaryTask task) {
        AiSummaryTaskRecord record = new AiSummaryTaskRecord();
        record.setId(task.id());
        record.setAlarmId(task.alarmId());
        record.setAlarmDedupKey(task.alarmDedupKey());
        record.setRuleCode(task.ruleCode());
        record.setDeviceId(task.deviceId());
        record.setSeverity(task.severity());
        record.setStatus(task.status().name());
        record.setAttemptCount(task.attemptCount());
        record.setSummary(task.summary());
        record.setPossibleCause(task.possibleCause());
        record.setInspectionSuggestion(task.inspectionSuggestion());
        record.setRiskLevel(task.riskLevel());
        record.setConfidence(task.confidence());
        record.setModelName(task.modelName());
        record.setPromptVersion(task.promptVersion());
        record.setRequestPayload(task.requestPayload());
        record.setResponsePayload(task.responsePayload());
        record.setErrorCode(task.errorCode());
        record.setErrorMessage(task.errorMessage());
        record.setAlarmTriggeredAt(task.alarmTriggeredAt());
        record.setCreatedAt(task.createdAt());
        record.setUpdatedAt(task.updatedAt());
        record.setStartedAt(task.startedAt());
        record.setFinishedAt(task.finishedAt());
        return record;
    }

    public AiSummaryTask toDomain() {
        return new AiSummaryTask(
                id,
                alarmId,
                alarmDedupKey,
                ruleCode,
                deviceId,
                severity,
                AiSummaryStatus.valueOf(status),
                attemptCount,
                summary,
                possibleCause,
                inspectionSuggestion,
                riskLevel,
                confidence,
                modelName,
                promptVersion,
                requestPayload,
                responsePayload,
                errorCode,
                errorMessage,
                alarmTriggeredAt,
                createdAt,
                updatedAt,
                startedAt,
                finishedAt);
    }
}
