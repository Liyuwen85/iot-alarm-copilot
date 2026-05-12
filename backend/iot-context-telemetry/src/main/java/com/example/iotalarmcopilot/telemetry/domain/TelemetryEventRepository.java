package com.example.iotalarmcopilot.telemetry.domain;

/**
 * 遥测领域持久化接口
 */
public interface TelemetryEventRepository {

    TelemetryEvent save(TelemetryEvent event);
}
