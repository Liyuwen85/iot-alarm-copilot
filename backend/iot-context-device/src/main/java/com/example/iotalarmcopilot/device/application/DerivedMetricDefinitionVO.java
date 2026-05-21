package com.example.iotalarmcopilot.device.application;

import java.util.List;

public record DerivedMetricDefinitionVO(
        String metricCode,
        List<String> sourceMetrics,
        String expression,
        boolean required,
        String unit) {
}
