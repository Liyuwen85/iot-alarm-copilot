package com.example.iotalarmcopilot.device.application;

import java.math.BigDecimal;

public record TelemetryMetricDefinitionVO(
        String metricCode,
        String sourcePath,
        BinaryStateMappingVO binaryStateMapping,
        String transformType,
        BigDecimal factor,
        BigDecimal offset,
        boolean required,
        String unit,
        BigDecimal minValue,
        BigDecimal maxValue) {
}
