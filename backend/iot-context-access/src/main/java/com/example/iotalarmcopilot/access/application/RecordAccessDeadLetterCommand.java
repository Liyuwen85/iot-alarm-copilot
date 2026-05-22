package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

public record RecordAccessDeadLetterCommand(
        String deadLetterTopic,
        String originalTopic,
        Integer originalPartition,
        Long originalOffset,
        String consumerGroup,
        String mqttTopic,
        String deviceId,
        String payload,
        String exceptionType,
        String exceptionMessage,
        Instant failedAt) {

    public RecordAccessDeadLetterCommand {
        if (deadLetterTopic == null || deadLetterTopic.isBlank()) {
            throw new BaseDomainException("deadLetterTopic must not be blank");
        }
        if (originalTopic == null || originalTopic.isBlank()) {
            throw new BaseDomainException("originalTopic must not be blank");
        }
        if (originalPartition == null || originalPartition < 0) {
            throw new BaseDomainException("originalPartition must not be negative");
        }
        if (originalOffset == null || originalOffset < 0) {
            throw new BaseDomainException("originalOffset must not be negative");
        }
        if (exceptionType == null || exceptionType.isBlank()) {
            throw new BaseDomainException("exceptionType must not be blank");
        }
        Objects.requireNonNull(failedAt, "failedAt must not be null");
    }
}
