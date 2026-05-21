package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.List;
import java.util.Objects;

/**
 * 派生指标定义
 *
 * @param capabilityCode 指标能力代码
 * @param sourceMetrics  来源指标代码集合
 * @param expression     计算表达式（目前仅支持SpELl）
 * @param required
 * @param unit
 */
public record DerivedMetricDefinition(
        CapabilityCode capabilityCode,
        List<CapabilityCode> sourceMetrics,
        String expression,
        boolean required,
        String unit) {

    public DerivedMetricDefinition {
        Objects.requireNonNull(capabilityCode, "capabilityCode must not be null");
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
        if (sourceMetrics.contains(capabilityCode)) {
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
