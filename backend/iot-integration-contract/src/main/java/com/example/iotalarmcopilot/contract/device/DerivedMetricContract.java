package com.example.iotalarmcopilot.contract.device;

import java.util.List;
import java.util.Objects;

/**
 * 派生指标
 */
public record DerivedMetricContract(
        String metricCode,
        List<String> sourceMetrics,
        String expression,
        boolean required,
        String unit) {

    public DerivedMetricContract {
        Objects.requireNonNull(metricCode, "metricCode must not be null");
        Objects.requireNonNull(sourceMetrics, "sourceMetrics must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        sourceMetrics = List.copyOf(sourceMetrics);
        if (metricCode.isBlank()) {
            throw new IllegalArgumentException("metricCode must not be blank");
        }
        if (sourceMetrics.isEmpty()) {
            throw new IllegalArgumentException("sourceMetrics must not be empty");
        }
        if (sourceMetrics.stream().anyMatch(metric -> metric == null || metric.isBlank())) {
            throw new IllegalArgumentException("sourceMetrics must not contain blank metric");
        }
        if (sourceMetrics.contains(metricCode)) {
            throw new IllegalArgumentException("derived metric must not depend on itself");
        }
        if (expression.isBlank()) {
            throw new IllegalArgumentException("expression must not be blank");
        }
        if (unit.isBlank()) {
            throw new IllegalArgumentException("unit must not be blank");
        }
    }
}
