package com.example.iotalarmcopilot.contract.device;

import java.util.Optional;

public interface DeviceTelemetryModelQueryPort {

    Optional<DeviceTelemetryModel> findTelemetryModel(String deviceCode);
}
