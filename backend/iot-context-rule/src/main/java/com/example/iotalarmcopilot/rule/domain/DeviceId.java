package com.example.iotalarmcopilot.rule.domain;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 设备标识值对象
 *
 * @param value
 */
public record DeviceId(String value) {

    public DeviceId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("deviceId must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
