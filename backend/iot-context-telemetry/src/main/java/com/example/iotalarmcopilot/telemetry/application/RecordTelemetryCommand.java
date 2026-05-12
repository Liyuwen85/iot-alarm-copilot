package com.example.iotalarmcopilot.telemetry.application;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 记录遥测数据命令
 */
public record RecordTelemetryCommand(
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        Instant reportedAt,
        String rawJson) {
}
