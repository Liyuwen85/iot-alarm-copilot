package com.example.iotalarmcopilot.ai.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * AI告警摘要任务聚合根
 *
 * @param id
 * @param alarmId              唯一
 * @param alarmDedupKey
 * @param ruleCode
 * @param deviceId
 * @param severity
 * @param status
 * @param attemptCount         调用重试次数
 * @param summary
 * @param possibleCause
 * @param inspectionSuggestion
 * @param riskLevel
 * @param confidence
 * @param modelName
 * @param promptVersion
 * @param requestPayload
 * @param responsePayload
 * @param errorCode
 * @param errorMessage
 * @param alarmTriggeredAt
 * @param createdAt
 * @param updatedAt
 * @param startedAt
 * @param finishedAt
 */
public record AiSummaryTask(
        Long id,
        Long alarmId,
        String alarmDedupKey,
        String ruleCode,
        String deviceId,
        String severity,
        AiSummaryStatus status,
        Integer attemptCount,
        String summary,
        String possibleCause,
        String inspectionSuggestion,
        String riskLevel,
        BigDecimal confidence,
        String modelName,
        String promptVersion,
        String requestPayload,
        String responsePayload,
        String errorCode,
        String errorMessage,
        Instant alarmTriggeredAt,
        Instant createdAt,
        Instant updatedAt,
        Instant startedAt,
        Instant finishedAt) {

    public static AiSummaryTask createPending(
            Long alarmId,
            String alarmDedupKey,
            String ruleCode,
            String deviceId,
            String severity,
            Instant alarmTriggeredAt,
            Instant createdAt) {
        return new AiSummaryTask(
                null,
                alarmId,
                alarmDedupKey,
                ruleCode,
                deviceId,
                severity,
                AiSummaryStatus.PENDING,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                alarmTriggeredAt,
                createdAt,
                createdAt,
                null,
                null);
    }

    /**
     * 认领任务
     *
     * @param startedAt
     * @return
     */
    public AiSummaryTask claim(Instant startedAt) {
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        if (status != AiSummaryStatus.PENDING) {
            throw new BaseDomainException("Only pending AI summary task can be claimed");
        }
        return new AiSummaryTask(
                id,
                alarmId,
                alarmDedupKey,
                ruleCode,
                deviceId,
                severity,
                AiSummaryStatus.PROCESSING,
                attemptCount + 1,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                requestPayload,
                responsePayload,
                null,
                null,
                alarmTriggeredAt,
                createdAt,
                startedAt,
                startedAt,
                null);
    }

    public AiSummaryTask succeed(
            AiStructuredSummary structuredSummary,
            String modelName,
            String promptVersion,
            String requestPayload,
            String responsePayload,
            Instant finishedAt) {
        Objects.requireNonNull(structuredSummary, "structuredSummary must not be null");
        Objects.requireNonNull(modelName, "modelName must not be null");
        Objects.requireNonNull(promptVersion, "promptVersion must not be null");
        Objects.requireNonNull(requestPayload, "requestPayload must not be null");
        Objects.requireNonNull(responsePayload, "responsePayload must not be null");
        Objects.requireNonNull(finishedAt, "finishedAt must not be null");
        if (status != AiSummaryStatus.PROCESSING) {
            throw new BaseDomainException("Only processing AI summary task can be completed");
        }
        return new AiSummaryTask(
                id,
                alarmId,
                alarmDedupKey,
                ruleCode,
                deviceId,
                severity,
                AiSummaryStatus.SUCCEEDED,
                attemptCount,
                structuredSummary.summary(),
                structuredSummary.possibleCause(),
                structuredSummary.inspectionSuggestion(),
                structuredSummary.riskLevel(),
                structuredSummary.confidence(),
                modelName,
                promptVersion,
                requestPayload,
                responsePayload,
                null,
                null,
                alarmTriggeredAt,
                createdAt,
                finishedAt,
                startedAt,
                finishedAt);
    }

    public AiSummaryTask fail(
            String modelName,
            String promptVersion,
            String requestPayload,
            String responsePayload,
            String errorCode,
            String errorMessage,
            Instant finishedAt) {
        Objects.requireNonNull(modelName, "modelName must not be null");
        Objects.requireNonNull(promptVersion, "promptVersion must not be null");
        Objects.requireNonNull(requestPayload, "requestPayload must not be null");
        Objects.requireNonNull(errorCode, "errorCode must not be null");
        Objects.requireNonNull(errorMessage, "errorMessage must not be null");
        Objects.requireNonNull(finishedAt, "finishedAt must not be null");
        if (status != AiSummaryStatus.PROCESSING) {
            throw new BaseDomainException("Only processing AI summary task can fail");
        }
        return new AiSummaryTask(
                id,
                alarmId,
                alarmDedupKey,
                ruleCode,
                deviceId,
                severity,
                AiSummaryStatus.FAILED,
                attemptCount,
                null,
                null,
                null,
                null,
                null,
                modelName,
                promptVersion,
                requestPayload,
                responsePayload,
                errorCode,
                errorMessage,
                alarmTriggeredAt,
                createdAt,
                finishedAt,
                startedAt,
                finishedAt);
    }

    public AiSummaryTask {
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(alarmDedupKey, "alarmDedupKey must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(attemptCount, "attemptCount must not be null");
        Objects.requireNonNull(alarmTriggeredAt, "alarmTriggeredAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        if (alarmDedupKey.isBlank()) {
            throw new BaseDomainException("alarmDedupKey must not be blank");
        }
        if (ruleCode.isBlank()) {
            throw new BaseDomainException("ruleCode must not be blank");
        }
        if (deviceId.isBlank()) {
            throw new BaseDomainException("deviceId must not be blank");
        }
        if (severity.isBlank()) {
            throw new BaseDomainException("severity must not be blank");
        }
        if (attemptCount < 0) {
            throw new BaseDomainException("attemptCount must not be negative");
        }
        if (status == AiSummaryStatus.PROCESSING && startedAt == null) {
            throw new BaseDomainException("startedAt must not be null when task is processing");
        }
        if (status == AiSummaryStatus.SUCCEEDED) {
            ensureSucceeded(summary, possibleCause, inspectionSuggestion, riskLevel, confidence, modelName, promptVersion, requestPayload, responsePayload, finishedAt);
        }
        if (status == AiSummaryStatus.FAILED) {
            ensureFailed(modelName, promptVersion, requestPayload, errorCode, errorMessage, finishedAt);
        }
    }

    private static void ensureSucceeded(
            String summary,
            String possibleCause,
            String inspectionSuggestion,
            String riskLevel,
            BigDecimal confidence,
            String modelName,
            String promptVersion,
            String requestPayload,
            String responsePayload,
            Instant finishedAt) {
        if (isBlank(summary)
                || isBlank(possibleCause)
                || isBlank(inspectionSuggestion)
                || isBlank(riskLevel)
                || confidence == null
                || isBlank(modelName)
                || isBlank(promptVersion)
                || isBlank(requestPayload)
                || isBlank(responsePayload)
                || finishedAt == null) {
            throw new BaseDomainException("Succeeded AI summary task is incomplete");
        }
    }

    private static void ensureFailed(
            String modelName,
            String promptVersion,
            String requestPayload,
            String errorCode,
            String errorMessage,
            Instant finishedAt) {
        if (isBlank(modelName)
                || isBlank(promptVersion)
                || isBlank(requestPayload)
                || isBlank(errorCode)
                || isBlank(errorMessage)
                || finishedAt == null) {
            throw new BaseDomainException("Failed AI summary task is incomplete");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
