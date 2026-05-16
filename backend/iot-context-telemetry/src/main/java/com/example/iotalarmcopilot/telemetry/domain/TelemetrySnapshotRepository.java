package com.example.iotalarmcopilot.telemetry.domain;

import java.util.Optional;

/**
 * 设备遥测快照仓库
 */
public interface TelemetrySnapshotRepository {

    void save(TelemetrySnapshot snapshot);

    Optional<TelemetrySnapshot> findByDeviceId(DeviceId deviceId);
}
