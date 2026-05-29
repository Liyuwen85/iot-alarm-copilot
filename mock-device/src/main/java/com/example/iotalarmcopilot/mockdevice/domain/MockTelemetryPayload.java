package com.example.iotalarmcopilot.mockdevice.domain;

import java.math.BigDecimal;

/**
 * 本地模拟设备发送的模拟数据
 */
public record MockTelemetryPayload(
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        String ts) {
}
