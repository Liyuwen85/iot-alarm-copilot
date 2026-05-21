package com.example.iotalarmcopilot.contract.device;

public interface DeviceTelemetryIngestionPort {

    void ensureTelemetryIngestionAllowed(String deviceCode);
}
