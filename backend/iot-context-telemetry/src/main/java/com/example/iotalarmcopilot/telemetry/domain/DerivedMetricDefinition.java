package com.example.iotalarmcopilot.telemetry.domain;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;

import java.util.List;
import java.util.Objects;

/**
 * 派生指标定义。如：heatIndex = temperature + (humidity * 0.1)
 *
 * @param metricName
 * @param sourceMetrics 来源指标
 * @param expression    计算表达式（目前SpEL）
 * @param required
 * @param unit
 */
public record DerivedMetricDefinition(
        TelemetryMetricName metricName,
        List<TelemetryMetricName> sourceMetrics,
        String expression,
        boolean required,
        String unit) {

    public DerivedMetricDefinition {
        Objects.requireNonNull(metricName, "metricName must not be null");
        Objects.requireNonNull(sourceMetrics, "sourceMetrics must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        sourceMetrics = List.copyOf(sourceMetrics);
        if (sourceMetrics.isEmpty()) {
            throw new BaseDomainException("sourceMetrics must not be empty");
        }
        if (sourceMetrics.stream().anyMatch(Objects::isNull)) {
            throw new BaseDomainException("sourceMetrics must not contain null metric");
        }
        if (sourceMetrics.contains(metricName)) {
            throw new BaseDomainException("derived metric must not depend on itself");
        }
        if (expression.isBlank()) {
            throw new BaseDomainException("expression must not be blank");
        }
        if (unit.isBlank()) {
            throw new BaseDomainException("unit must not be blank");
        }
    }
}
