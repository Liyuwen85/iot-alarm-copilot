package com.example.iotalarmcopilot.device.application;

import java.time.Instant;

public record UpdateDeviceShadowCommand(String deviceCode, String shadowDocument, Instant updatedAt) {
}
