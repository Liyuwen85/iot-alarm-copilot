package com.example.iotalarmcopilot.device.application;

import java.time.Instant;

public record RegisterDeviceCommand(
        String deviceCode,
        String productCode,
        String deviceName,
        String groupCode,
        Instant registeredAt) {
}
