package com.example.iotalarmcopilot.command.application;

import java.time.Instant;

/**
 * 发送给设备的"上报时间命令"
 */
public record SendSetReportIntervalCommand(
        String deviceId,
        int intervalMs,
        Instant requestedAt) {
}
