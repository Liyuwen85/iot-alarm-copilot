package com.example.iotalarmcopilot.device.application;

import java.time.Instant;

public record FinishMaintenanceCommand(String deviceCode, Instant finishedAt) {
}
