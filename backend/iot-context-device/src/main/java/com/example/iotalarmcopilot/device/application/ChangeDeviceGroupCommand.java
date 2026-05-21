package com.example.iotalarmcopilot.device.application;

import java.time.Instant;

public record ChangeDeviceGroupCommand(String deviceCode, String groupCode, Instant changedAt) {
}
