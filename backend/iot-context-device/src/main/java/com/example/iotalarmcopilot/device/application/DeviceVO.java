package com.example.iotalarmcopilot.device.application;

import java.time.Instant;

public record DeviceVO(
        Long id,
        String deviceCode,
        String productCode,
        String deviceName,
        String groupCode,
        String status,
        Long shadowVersion,
        String shadowDocument,
        Instant shadowUpdatedAt,
        Instant registeredAt,
        Instant statusChangedAt,
        Instant createdAt,
        Instant updatedAt) {
}
