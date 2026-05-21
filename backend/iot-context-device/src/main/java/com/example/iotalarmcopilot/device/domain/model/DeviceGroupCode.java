package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 设备分组值对象
 * @param value
 */
public record DeviceGroupCode(String value) {

    public DeviceGroupCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("groupCode must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
