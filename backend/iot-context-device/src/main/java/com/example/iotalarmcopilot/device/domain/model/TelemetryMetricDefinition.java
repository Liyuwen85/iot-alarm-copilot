package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 基础指标定义（如何从设备原始上报中抽取、归一、校验对应的指标）
 * 注，与telemetry中的TelemetryMetricDefinition不同，telemetry中定义的遥测事实。这里为“产品允许平台如何理解这些事实”
 *
 * @param capabilityCode
 * @param sourcePath         标明从原始报文的哪个路径中获取
 * @param binaryStateMapping 二值状态类指标映射
 * @param transformType      原值转换类型
 * @param factor             transformType 为 SCALE 时，用于系数乘
 * @param offset             transformType 为 OFFSET 时，用于加减
 * @param required
 * @param unit
 * @param minValue
 * @param maxValue
 */
public record TelemetryMetricDefinition(
        CapabilityCode capabilityCode,
        String sourcePath,
        BinaryStateMapping binaryStateMapping,
        TelemetryTransformType transformType,
        BigDecimal factor,
        BigDecimal offset,
        boolean required,
        String unit,
        BigDecimal minValue,
        BigDecimal maxValue) {

    public TelemetryMetricDefinition {
        Objects.requireNonNull(capabilityCode, "capabilityCode must not be null");
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(transformType, "transformType must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        if (sourcePath.isBlank()) {
            throw new BaseDomainException("sourcePath must not be blank");
        }
        if (!sourcePath.startsWith("/")) {
            throw new BaseDomainException("sourcePath must start with '/'");
        }
        if (binaryStateMapping != null && transformType != TelemetryTransformType.IDENTITY) {
            throw new BaseDomainException("binaryStateMapping only supports IDENTITY transform");
        }
        if (transformType == TelemetryTransformType.SCALE && factor == null) {
            throw new BaseDomainException("factor must not be null when transformType is SCALE");
        }
        if (transformType == TelemetryTransformType.OFFSET && offset == null) {
            throw new BaseDomainException("offset must not be null when transformType is OFFSET");
        }
        if (binaryStateMapping != null && (factor != null || offset != null)) {
            throw new BaseDomainException("binaryStateMapping does not support factor or offset");
        }
        if (unit.isBlank()) {
            throw new BaseDomainException("unit must not be blank");
        }
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new BaseDomainException("minValue must not be greater than maxValue");
        }
    }
}
