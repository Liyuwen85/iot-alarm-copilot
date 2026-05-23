package com.example.iotalarmcopilot.contract.device;

/**
 * 允许设备上报数据
 */
public interface DeviceTelemetryIngestionPort {

    /**
     * 确保设备允许上报数据
     *
     * @param deviceCode 设备码
     */
    void ensureTelemetryIngestionAllowed(String deviceCode);
}
