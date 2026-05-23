package com.example.iotalarmcopilot.contract.device;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 基础指标
 */
public record TelemetryMetricContract(
        String metricCode,
        String sourcePath,
        BinaryStateMappingContract binaryStateMapping,
        TelemetryTransformType transformType,
        BigDecimal factor,
        BigDecimal offset,
        boolean required,
        String unit,
        BigDecimal minValue,
        BigDecimal maxValue) {

    public TelemetryMetricContract {
        Objects.requireNonNull(metricCode, "metricCode must not be null");
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(transformType, "transformType must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        if (metricCode.isBlank()) {
            throw new IllegalArgumentException("metricCode must not be blank");
        }
        if (sourcePath.isBlank()) {
            throw new IllegalArgumentException("sourcePath must not be blank");
        }
        if (!sourcePath.startsWith("/")) {
            throw new IllegalArgumentException("sourcePath must start with '/'");
        }
        if (binaryStateMapping != null && transformType != TelemetryTransformType.IDENTITY) {
            throw new IllegalArgumentException("binaryStateMapping only supports IDENTITY transform");
        }
        if (transformType == TelemetryTransformType.SCALE && factor == null) {
            throw new IllegalArgumentException("factor must not be null when transformType is SCALE");
        }
        if (transformType == TelemetryTransformType.OFFSET && offset == null) {
            throw new IllegalArgumentException("offset must not be null when transformType is OFFSET");
        }
        if (binaryStateMapping != null && (factor != null || offset != null)) {
            throw new IllegalArgumentException("binaryStateMapping does not support factor or offset");
        }
        if (unit.isBlank()) {
            throw new IllegalArgumentException("unit must not be blank");
        }
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new IllegalArgumentException("minValue must not be greater than maxValue");
        }
    }
}
