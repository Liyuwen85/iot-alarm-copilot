package com.example.iotalarmcopilot.device.application;

import java.time.Instant;

public record ActivateDeviceCommand(String deviceCode, Instant activatedAt) {
}
