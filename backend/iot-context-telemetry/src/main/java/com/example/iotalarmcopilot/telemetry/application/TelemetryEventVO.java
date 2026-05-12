package com.example.iotalarmcopilot.telemetry.application;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 遥测事件值VO
 */
public record TelemetryEventVO(
        Long id,
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        Instant reportedAt,
        String rawJson) {
}
