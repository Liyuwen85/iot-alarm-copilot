package com.example.iotalarmcopilot.device.domain.policy;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.device.domain.model.DeviceStatus;

import java.time.Instant;

/**
 * 设备状态策略
 */
public final class DeviceStatusPolicy {

    private DeviceStatusPolicy() {
    }

    /**
     * 验证生命周期是否正常
     *
     * @param status
     * @param registeredAt
     * @param statusChangedAt
     */
    public static void validateLifecycle(
            DeviceStatus status,
            Instant registeredAt,
            Instant statusChangedAt) {
        if (statusChangedAt.isBefore(registeredAt)) {
            throw new BaseDomainException("statusChangedAt must not be before registeredAt");
        }
    }

    /**
     * 确保设备处于注册状态
     *
     * @param status
     */
    public static void ensureActivateAllowed(DeviceStatus status) {
        if (status != DeviceStatus.REGISTERED) {
            throw new BaseDomainException("Only registered device can be activated");
        }
    }

    /**
     * 确保设备处于激活状态
     *
     * @param status
     */
    public static void ensureStartMaintenanceAllowed(DeviceStatus status) {
        if (status != DeviceStatus.ACTIVATED) {
            throw new BaseDomainException("Only activated device can enter maintenance");
        }
    }

    /**
     * 确保设备处于维保状态
     *
     * @param status
     */
    public static void ensureFinishMaintenanceAllowed(DeviceStatus status) {
        if (status != DeviceStatus.MAINTENANCE) {
            throw new BaseDomainException("Only maintenance device can finish maintenance");
        }
    }

    /**
     * 确保设备处于禁用状态
     *
     * @param status
     */
    public static void ensureDisableAllowed(DeviceStatus status) {
        if (status != DeviceStatus.ACTIVATED && status != DeviceStatus.MAINTENANCE) {
            throw new BaseDomainException("Only activated or maintenance device can be disabled");
        }
    }

    /**
     * 确保设备处于可用状态
     *
     * @param status
     */
    public static void ensureRetireAllowed(DeviceStatus status) {
        if (status == DeviceStatus.RETIRED) {
            throw new BaseDomainException("Only non-retired device can be retired");
        }
    }

    /**
     * 确保设备可修改
     *
     * @param status
     */
    public static void ensureMutable(DeviceStatus status) {
        if (status == DeviceStatus.DISABLED || status == DeviceStatus.RETIRED) {
            throw new BaseDomainException("Device is not mutable in current status");
        }
    }
}
