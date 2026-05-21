package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.List;
import java.util.Objects;

/**
 * 描述设备可被调用的动作
 *
 * @param serviceCode
 * @param serviceName
 * @param inputCapabilities
 */
public record ThingServiceDefinition(
        String serviceCode,
        String serviceName,
        List<CapabilityCode> inputCapabilities) {

    public ThingServiceDefinition {
        Objects.requireNonNull(serviceCode, "serviceCode must not be null");
        Objects.requireNonNull(serviceName, "serviceName must not be null");
        Objects.requireNonNull(inputCapabilities, "inputCapabilities must not be null");
        inputCapabilities = List.copyOf(inputCapabilities);
        if (serviceCode.isBlank()) {
            throw new BaseDomainException("serviceCode must not be blank");
        }
        if (serviceName.isBlank()) {
            throw new BaseDomainException("serviceName must not be blank");
        }
        if (inputCapabilities.stream().anyMatch(Objects::isNull)) {
            throw new BaseDomainException("inputCapabilities must not contain null capability");
        }
    }
}
