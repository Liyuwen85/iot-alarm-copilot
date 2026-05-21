package com.example.iotalarmcopilot.device.application;

import java.time.Instant;

public record DisableDeviceCommand(String deviceCode, Instant disabledAt) {
}
