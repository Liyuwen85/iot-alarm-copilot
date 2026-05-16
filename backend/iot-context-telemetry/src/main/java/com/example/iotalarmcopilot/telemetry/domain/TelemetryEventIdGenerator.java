package com.example.iotalarmcopilot.telemetry.domain;

import java.time.Instant;

/**
 * 生成遥测事件id
 */
public interface TelemetryEventIdGenerator {

    Long nextId(String deviceId, Instant reportedAt, String rawJson);
}
