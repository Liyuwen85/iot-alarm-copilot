package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.telemetry.domain.TelemetrySchema;

/**
 * 解析设备ID对应的遥测模型
 */
public interface TelemetrySchemaResolver {

    TelemetrySchema resolveByDeviceId(String deviceId);
}
