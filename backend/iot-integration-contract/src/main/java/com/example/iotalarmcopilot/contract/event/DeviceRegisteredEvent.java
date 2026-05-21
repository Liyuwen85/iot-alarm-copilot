package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * 设备分组变更事件
 *
 * @param deviceCode
 * @param productCode
 * @param deviceName
 * @param groupCode
 * @param registeredAt
 */
public record DeviceRegisteredEvent(
        String deviceCode,
        String productCode,
        String deviceName,
        String groupCode,
        Instant registeredAt) implements DomainEvent {

    public static final String EVENT_TYPE = "device.registered";

    public DeviceRegisteredEvent {
        Objects.requireNonNull(deviceCode, "deviceCode must not be null");
        Objects.requireNonNull(productCode, "productCode must not be null");
        Objects.requireNonNull(deviceName, "deviceName must not be null");
        Objects.requireNonNull(registeredAt, "registeredAt must not be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant occurredAt() {
        return registeredAt;
    }
}
