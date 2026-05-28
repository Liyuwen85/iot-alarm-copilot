package com.example.iotalarmcopilot.command.application;

import java.time.Instant;

/**
 * 命令确认消息负载
 */
public record CommandAckPayload(
        String commandId,
        String deviceId,
        String status,
        Instant ackedAt,
        String message) {
}
