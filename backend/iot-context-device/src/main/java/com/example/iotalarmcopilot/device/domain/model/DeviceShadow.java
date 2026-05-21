package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * 设备影子
 *
 * @param version
 * @param document
 * @param updatedAt
 */
public record DeviceShadow(Long version, String document, Instant updatedAt) {

    public DeviceShadow {
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        if (version < 1) {
            throw new BaseDomainException("shadow version must be positive");
        }
        if (document.isBlank()) {
            throw new BaseDomainException("shadow document must not be blank");
        }
    }

    public static DeviceShadow create(String document, Instant updatedAt) {
        return new DeviceShadow(1L, document, updatedAt);
    }

    public DeviceShadow update(String document, Instant updatedAt) {
        return new DeviceShadow(version + 1, document, updatedAt);
    }
}
