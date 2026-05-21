package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 产品代码值对象
 *
 * @param value
 */
public record ProductCode(String value) {

    public ProductCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("productCode must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
