package com.example.iotalarmcopilot.command.application;

import java.time.Instant;

public record DeviceCommandVO(
        Long id,
        String commandId,
        String deviceId,
        String commandType,
        String payloadJson,
        String status,
        String ackMessage,
        Instant sentAt,
        Instant ackedAt,
        Instant createdAt,
        Instant updatedAt) {
}
