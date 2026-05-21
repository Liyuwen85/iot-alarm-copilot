package com.example.iotalarmcopilot.device.infrastructure.persistence;

import com.example.iotalarmcopilot.device.domain.model.*;
import lombok.Data;

import java.time.Instant;

/**
 * 设备记录数据库实体
 */
@Data
public class DeviceRecord {

    private Long id;
    private String deviceCode;
    private String productCode;
    private String deviceName;
    private String groupCode;
    private String status;
    private Long shadowVersion;
    private String shadowDocument;
    private Instant shadowUpdatedAt;
    private Instant registeredAt;
    private Instant statusChangedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static DeviceRecord fromDomain(Device device) {
        DeviceRecord record = new DeviceRecord();
        record.setId(device.id());
        record.setDeviceCode(device.deviceCode().value());
        record.setProductCode(device.productCode().value());
        record.setDeviceName(device.deviceName());
        record.setGroupCode(device.groupCodeValue());
        record.setStatus(device.status().name());
        record.setShadowVersion(device.shadowVersion());
        record.setShadowDocument(device.shadowDocument());
        record.setShadowUpdatedAt(device.shadowUpdatedAt());
        record.setRegisteredAt(device.registeredAt());
        record.setStatusChangedAt(device.statusChangedAt());
        record.setCreatedAt(device.createdAt());
        record.setUpdatedAt(device.updatedAt());
        return record;
    }

    public Device toDomain() {
        DeviceShadow shadow = shadowVersion == null || shadowDocument == null || shadowUpdatedAt == null
                ? null
                : new DeviceShadow(shadowVersion, shadowDocument, shadowUpdatedAt);
        return new Device(
                id,
                new DeviceCode(deviceCode),
                new ProductCode(productCode),
                deviceName,
                groupCode == null || groupCode.isBlank() ? null : new DeviceGroupCode(groupCode),
                DeviceStatus.valueOf(status),
                shadow,
                registeredAt,
                statusChangedAt,
                createdAt,
                updatedAt);
    }
}
