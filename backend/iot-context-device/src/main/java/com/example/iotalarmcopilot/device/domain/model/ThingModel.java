package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 物模型（简单示意）
 *
 * @param version
 * @param properties
 * @param events
 * @param services
 */
public record ThingModel(
        ThingModelVersion version,
        List<ThingPropertyDefinition> properties,
        List<ThingEventDefinition> events,
        List<ThingServiceDefinition> services) {

    public static ThingModel empty() {
        return new ThingModel(new ThingModelVersion(1), List.of(), List.of(), List.of());
    }

    public ThingModel {
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(events, "events must not be null");
        Objects.requireNonNull(services, "services must not be null");
        properties = List.copyOf(properties);
        events = List.copyOf(events);
        services = List.copyOf(services);
        if (properties.stream().anyMatch(Objects::isNull)) {
            throw new BaseDomainException("properties must not contain null definition");
        }
        if (events.stream().anyMatch(Objects::isNull)) {
            throw new BaseDomainException("events must not contain null definition");
        }
        if (services.stream().anyMatch(Objects::isNull)) {
            throw new BaseDomainException("services must not contain null definition");
        }
        ensureUniquePropertyCodes(properties);
        ensureUniqueEventCodes(events);
        ensureUniqueServiceCodes(services);
    }

    private static void ensureUniquePropertyCodes(List<ThingPropertyDefinition> properties) {
        Set<CapabilityCode> propertyCodes = new HashSet<>();
        for (ThingPropertyDefinition property : properties) {
            if (!propertyCodes.add(property.capabilityCode())) {
                throw new BaseDomainException("thing properties must not contain duplicate capability: "
                        + property.capabilityCode().value());
            }
        }
    }

    private static void ensureUniqueEventCodes(List<ThingEventDefinition> events) {
        Set<String> eventCodes = new HashSet<>();
        for (ThingEventDefinition event : events) {
            if (!eventCodes.add(event.eventCode())) {
                throw new BaseDomainException("thing events must not contain duplicate eventCode: "
                        + event.eventCode());
            }
        }
    }

    private static void ensureUniqueServiceCodes(List<ThingServiceDefinition> services) {
        Set<String> serviceCodes = new HashSet<>();
        for (ThingServiceDefinition service : services) {
            if (!serviceCodes.add(service.serviceCode())) {
                throw new BaseDomainException("thing services must not contain duplicate serviceCode: "
                        + service.serviceCode());
            }
        }
    }
}
