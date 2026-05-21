package com.example.iotalarmcopilot.device.application;

import java.time.Instant;

public record RetireDeviceCommand(String deviceCode, Instant retiredAt) {
}
