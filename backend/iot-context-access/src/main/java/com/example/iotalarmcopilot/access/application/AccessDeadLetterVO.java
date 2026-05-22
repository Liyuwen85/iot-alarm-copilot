package com.example.iotalarmcopilot.access.application;

import java.time.Instant;

public record AccessDeadLetterVO(
        Long id,
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
        Instant failedAt,
        Instant createdAt) {
}
