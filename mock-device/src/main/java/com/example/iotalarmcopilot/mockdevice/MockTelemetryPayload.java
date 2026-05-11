package com.example.iotalarmcopilot.mockdevice;

import java.math.BigDecimal;

public record MockTelemetryPayload(
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        String ts) {
}
