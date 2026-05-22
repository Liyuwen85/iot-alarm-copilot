package com.example.iotalarmcopilot.telemetry.domain;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 基础指标定义
 *
 * @param metricName
 * @param required
 * @param unit
 * @param minValue
 * @param maxValue
 */
public record TelemetryMetricDefinition(
        TelemetryMetricName metricName,
        boolean required,
        String unit,
        BigDecimal minValue,
        BigDecimal maxValue) {

    public TelemetryMetricDefinition {
        Objects.requireNonNull(metricName, "metricName must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        if (unit.isBlank()) {
            throw new BaseDomainException("unit must not be blank");
        }
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new BaseDomainException("minValue must not be greater than maxValue");
        }
    }

    /**
     * 验证指标值是否满足定义
     *
     * @param value
     * @param productCode
     */
    public void validate(java.math.BigDecimal value, String productCode) {
        if (value == null) {
            if (required) {
                throw new BaseDomainException("required telemetry metric is missing. productCode=" + productCode + ", metric=" + metricName.value());
            }
            return;
        }
        if (minValue != null && value.compareTo(minValue) < 0) {
            throw new BaseDomainException("telemetry metric is below minimum. productCode=" + productCode + ", metric=" + metricName.value());
        }
        if (maxValue != null && value.compareTo(maxValue) > 0) {
            throw new BaseDomainException("telemetry metric is above maximum. productCode=" + productCode + ", metric=" + metricName.value());
        }
    }
}
