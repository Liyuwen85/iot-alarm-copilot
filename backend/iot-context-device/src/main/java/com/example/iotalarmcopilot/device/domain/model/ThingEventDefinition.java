package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.List;
import java.util.Objects;

/**
 * 设备事件定义。标识设备主动上报哪些事实，以及这个事实携带了哪些能力。
 *
 * @param eventCode
 * @param eventName
 * @param outputCapabilities
 */
public record ThingEventDefinition(
        String eventCode,
        String eventName,
        List<CapabilityCode> outputCapabilities) {

    public ThingEventDefinition {
        Objects.requireNonNull(eventCode, "eventCode must not be null");
        Objects.requireNonNull(eventName, "eventName must not be null");
        Objects.requireNonNull(outputCapabilities, "outputCapabilities must not be null");
        outputCapabilities = List.copyOf(outputCapabilities);
        if (eventCode.isBlank()) {
            throw new BaseDomainException("eventCode must not be blank");
        }
        if (eventName.isBlank()) {
            throw new BaseDomainException("eventName must not be blank");
        }
        if (outputCapabilities.stream().anyMatch(Objects::isNull)) {
            throw new BaseDomainException("outputCapabilities must not contain null capability");
        }
    }
}
