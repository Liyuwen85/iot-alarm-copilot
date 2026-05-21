package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.device.domain.policy.DeviceStatusPolicy;

import java.time.Instant;
import java.util.Objects;

/**
 * 设备模型聚合根。管理设备的身份、生命周期、设备分组、影子快照等。
 *
 * @param id
 * @param deviceCode
 * @param productCode
 * @param deviceName
 * @param groupCode
 * @param status
 * @param shadow          当前影子快照（可空）
 * @param registeredAt
 * @param statusChangedAt
 * @param createdAt
 * @param updatedAt
 */
public record Device(
        Long id,
        DeviceCode deviceCode,
        ProductCode productCode,
        String deviceName,
        DeviceGroupCode groupCode,
        DeviceStatus status,
        DeviceShadow shadow,
        Instant registeredAt,
        Instant statusChangedAt,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * 注册设备。
     *
     * @param deviceCode
     * @param productCode
     * @param deviceName
     * @param groupCode
     * @param registeredAt
     * @return
     */
    public static Device register(
            DeviceCode deviceCode,
            ProductCode productCode,
            String deviceName,
            DeviceGroupCode groupCode,
            Instant registeredAt) {
        return new Device(
                null,
                deviceCode,
                productCode,
                deviceName,
                groupCode,
                DeviceStatus.REGISTERED,
                null,
                registeredAt,
                registeredAt,
                registeredAt,
                registeredAt);
    }

    /**
     * 设备激活。
     *
     * @param activatedAt
     * @return
     */
    public Device activate(Instant activatedAt) {
        Objects.requireNonNull(activatedAt, "activatedAt must not be null");
        DeviceStatusPolicy.ensureActivateAllowed(status);
        return touch(DeviceStatus.ACTIVATED, activatedAt);
    }

    /**
     * 设备开始维护。
     *
     * @param startedAt
     * @return
     */
    public Device startMaintenance(Instant startedAt) {
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        DeviceStatusPolicy.ensureStartMaintenanceAllowed(status);
        return touch(DeviceStatus.MAINTENANCE, startedAt);
    }

    /**
     * 设备结束维护。
     *
     * @param finishedAt
     * @return
     */
    public Device finishMaintenance(Instant finishedAt) {
        Objects.requireNonNull(finishedAt, "finishedAt must not be null");
        DeviceStatusPolicy.ensureFinishMaintenanceAllowed(status);
        return touch(DeviceStatus.ACTIVATED, finishedAt);
    }

    /**
     * 设备禁用。
     *
     * @param disabledAt
     * @return
     */
    public Device disable(Instant disabledAt) {
        Objects.requireNonNull(disabledAt, "disabledAt must not be null");
        DeviceStatusPolicy.ensureDisableAllowed(status);
        return touch(DeviceStatus.DISABLED, disabledAt);
    }

    /**
     * 设备退役。
     *
     * @param retiredAt
     * @return
     */
    public Device retire(Instant retiredAt) {
        Objects.requireNonNull(retiredAt, "retiredAt must not be null");
        DeviceStatusPolicy.ensureRetireAllowed(status);
        return touch(DeviceStatus.RETIRED, retiredAt);
    }

    /**
     * 设备更换分组。
     *
     * @param newGroupCode
     * @param changedAt
     * @return
     */
    public Device changeGroup(DeviceGroupCode newGroupCode, Instant changedAt) {
        Objects.requireNonNull(newGroupCode, "newGroupCode must not be null");
        Objects.requireNonNull(changedAt, "changedAt must not be null");
        DeviceStatusPolicy.ensureMutable(status);
        if (Objects.equals(groupCode, newGroupCode)) {
            return this;
        }
        return new Device(
                id,
                deviceCode,
                productCode,
                deviceName,
                newGroupCode,
                status,
                shadow,
                registeredAt,
                statusChangedAt,
                createdAt,
                changedAt);
    }

    /**
     * 更新影子。
     *
     * @param document
     * @param updatedAt
     * @return
     */
    public Device updateShadow(String document, Instant updatedAt) {
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        DeviceStatusPolicy.ensureMutable(status);
        DeviceShadow newShadow = shadow == null
                ? DeviceShadow.create(document, updatedAt)
                : shadow.update(document, updatedAt);
        return new Device(
                id,
                deviceCode,
                productCode,
                deviceName,
                groupCode,
                status,
                newShadow,
                registeredAt,
                statusChangedAt,
                createdAt,
                updatedAt);
    }

    public String groupCodeValue() {
        return groupCode == null ? null : groupCode.value();
    }

    public String shadowDocument() {
        return shadow == null ? null : shadow.document();
    }

    public Long shadowVersion() {
        return shadow == null ? null : shadow.version();
    }

    public Instant shadowUpdatedAt() {
        return shadow == null ? null : shadow.updatedAt();
    }

    public Device {
        Objects.requireNonNull(deviceCode, "deviceCode must not be null");
        Objects.requireNonNull(productCode, "productCode must not be null");
        Objects.requireNonNull(deviceName, "deviceName must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(registeredAt, "registeredAt must not be null");
        Objects.requireNonNull(statusChangedAt, "statusChangedAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        if (deviceName.isBlank()) {
            throw new BaseDomainException("deviceName must not be blank");
        }
        DeviceStatusPolicy.validateLifecycle(status, registeredAt, statusChangedAt);
    }

    private Device touch(DeviceStatus newStatus, Instant changedAt) {
        return new Device(
                id,
                deviceCode,
                productCode,
                deviceName,
                groupCode,
                newStatus,
                shadow,
                registeredAt,
                changedAt,
                createdAt,
                changedAt);
    }
}
