package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * 设备分组变更事件
 *
 * @param deviceCode
 * @param oldGroupCode
 * @param newGroupCode
 * @param changedAt
 */
public record DeviceGroupChangedEvent(
        String deviceCode,
        String oldGroupCode,
        String newGroupCode,
        Instant changedAt) implements DomainEvent {

    public static final String EVENT_TYPE = "device.group.changed";

    public DeviceGroupChangedEvent {
        Objects.requireNonNull(deviceCode, "deviceCode must not be null");
        Objects.requireNonNull(newGroupCode, "newGroupCode must not be null");
        Objects.requireNonNull(changedAt, "changedAt must not be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant occurredAt() {
        return changedAt;
    }
}
