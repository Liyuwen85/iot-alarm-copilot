package com.example.iotalarmcopilot.device.application;

public record ThingPropertyDefinitionVO(
        String propertyCode,
        String source,
        String accessMode,
        String dataType,
        String unit) {
}
