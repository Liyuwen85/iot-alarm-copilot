package com.example.iotalarmcopilot.device.domain.repository;

import com.example.iotalarmcopilot.device.domain.model.Device;

import java.util.Objects;

/**
 * 设备更新结果
 *
 * @param device
 * @param changed
 */
public record DeviceUpdateResult(
        Device device,
        boolean changed) {

    public DeviceUpdateResult {
        Objects.requireNonNull(device, "device must not be null");
    }
}
