package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 属性定义
 *
 * @param capabilityCode
 * @param source
 * @param accessMode
 * @param dataType
 * @param unit
 */
public record ThingPropertyDefinition(
        CapabilityCode capabilityCode,
        ThingPropertySource source,
        ThingPropertyAccessMode accessMode,
        ThingDataType dataType,
        String unit) {

    public ThingPropertyDefinition {
        Objects.requireNonNull(capabilityCode, "capabilityCode must not be null");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(accessMode, "accessMode must not be null");
        Objects.requireNonNull(dataType, "dataType must not be null");
        if (unit != null && unit.isBlank()) {
            throw new BaseDomainException("unit must not be blank");
        }
    }
}
