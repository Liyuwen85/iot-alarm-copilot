package com.example.iotalarmcopilot.telemetry.application.port;

import java.time.Instant;

/**
 * 遥测事件id生成器
 */
public interface TelemetryEventIdGenerator {

    Long nextId(String deviceId, Instant reportedAt, String rawJson);
}
