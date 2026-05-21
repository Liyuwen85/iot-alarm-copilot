package com.example.iotalarmcopilot.device.domain.repository;

import com.example.iotalarmcopilot.device.domain.model.Device;

/**
 * 设备保存结果
 *
 * @param device
 * @param created
 */
public record DeviceSaveResult(Device device, boolean created) {
}
