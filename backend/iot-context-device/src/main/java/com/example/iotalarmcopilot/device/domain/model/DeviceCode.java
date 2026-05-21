package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 设备代码值对象
 * @param value
 */
public record DeviceCode(String value) {

    public DeviceCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("deviceCode must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
