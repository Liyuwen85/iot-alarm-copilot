package com.example.iotalarmcopilot.device.application;

import java.time.Instant;
import java.util.List;

public record ProductModelVO(
        Long id,
        String productCode,
        String productName,
        List<String> capabilities,
        List<TelemetryMetricDefinitionVO> telemetryMetrics,
        List<DerivedMetricDefinitionVO> derivedTelemetryMetrics,
        Integer thingModelVersion,
        List<ThingPropertyDefinitionVO> thingProperties,
        List<ThingEventDefinitionVO> thingEvents,
        List<ThingServiceDefinitionVO> thingServices,
        List<String> reportedFields,
        List<String> desiredFields,
        Instant createdAt,
        Instant updatedAt) {
}
