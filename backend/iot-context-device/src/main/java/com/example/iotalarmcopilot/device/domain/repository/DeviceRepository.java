package com.example.iotalarmcopilot.device.domain.repository;

import com.example.iotalarmcopilot.device.domain.model.Device;
import com.example.iotalarmcopilot.device.domain.model.DeviceCode;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓储
 */
public interface DeviceRepository {

    DeviceSaveResult saveIfAbsent(Device device);

    DeviceUpdateResult updateIfUnchanged(Device current, Device target);

    Optional<Device> findByDeviceCode(DeviceCode deviceCode);

    List<Device> findRecent(int limit);
}
