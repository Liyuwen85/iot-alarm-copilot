package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * 设备状态变更事件
 */
public record DeviceStatusChangedEvent(
        String deviceCode,
        String status,
        Instant changedAt) implements DomainEvent {

    public static final String EVENT_TYPE = "device.status.changed";

    public DeviceStatusChangedEvent {
        Objects.requireNonNull(deviceCode, "deviceCode must not be null");
        Objects.requireNonNull(status, "status must not be null");
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
