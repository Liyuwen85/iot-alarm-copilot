package com.example.iotalarmcopilot.device.interfaces;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryIngestionPort;
import com.example.iotalarmcopilot.device.domain.model.Device;
import com.example.iotalarmcopilot.device.domain.model.DeviceCode;
import com.example.iotalarmcopilot.device.domain.model.DeviceStatus;
import com.example.iotalarmcopilot.device.domain.repository.DeviceRepository;
import org.springframework.stereotype.Component;

/**
 * 设备遥测数据入库守卫
 */
@Component
public class DeviceTelemetryIngestionGuard implements DeviceTelemetryIngestionPort {

    private final DeviceRepository deviceRepository;

    public DeviceTelemetryIngestionGuard(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * 在入库前，确保设备是否允许接收上行数据
     *
     * @param deviceCode 设备代码
     */
    @Override
    public void ensureTelemetryIngestionAllowed(String deviceCode) {
        Device device = deviceRepository.findByDeviceCode(new DeviceCode(deviceCode))
                .orElseThrow(() -> new BaseDomainException("Device not found. deviceCode=" + deviceCode));
        if (device.status() == DeviceStatus.DISABLED || device.status() == DeviceStatus.RETIRED) {
            throw new BaseDomainException("Device is not allowed to ingest telemetry. deviceCode=" + deviceCode);
        }
    }
}
