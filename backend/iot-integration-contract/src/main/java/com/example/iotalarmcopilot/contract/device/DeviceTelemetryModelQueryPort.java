package com.example.iotalarmcopilot.contract.device;

import java.util.Optional;

/**
 * 查询设备模型
 */
public interface DeviceTelemetryModelQueryPort {

    Optional<DeviceTelemetryModel> findTelemetryModel(String deviceCode);
}
