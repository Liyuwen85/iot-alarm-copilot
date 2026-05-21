package com.example.iotalarmcopilot.device.application;

import java.time.Instant;

public record StartMaintenanceCommand(String deviceCode, Instant startedAt) {
}
