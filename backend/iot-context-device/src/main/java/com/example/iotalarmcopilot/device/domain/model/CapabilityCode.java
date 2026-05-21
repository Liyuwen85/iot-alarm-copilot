package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 设备能力在平台中的统一标识
 *
 * @param value
 */
public record CapabilityCode(String value) {

    public CapabilityCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("capabilityCode must not be blank");
        }
    }
}
