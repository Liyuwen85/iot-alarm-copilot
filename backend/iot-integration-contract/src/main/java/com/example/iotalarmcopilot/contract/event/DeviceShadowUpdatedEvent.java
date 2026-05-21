package com.example.iotalarmcopilot.contract.event;

import com.example.iotalarmcopilot.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * 设备分组变更事件
 */
public record DeviceShadowUpdatedEvent(
        String deviceCode,
        Long shadowVersion,
        String shadowDocument,
        Instant updatedAt) implements DomainEvent {

    public static final String EVENT_TYPE = "device.shadow.updated";

    public DeviceShadowUpdatedEvent {
        Objects.requireNonNull(deviceCode, "deviceCode must not be null");
        Objects.requireNonNull(shadowVersion, "shadowVersion must not be null");
        Objects.requireNonNull(shadowDocument, "shadowDocument must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant occurredAt() {
        return updatedAt;
    }
}
