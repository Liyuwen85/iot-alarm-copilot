package com.example.iotalarmcopilot.telemetry.domain;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;

import java.util.List;
import java.util.Objects;

/**
 * 遥测Schema，用于指标的定义和验证（主要为了配合演示 “物模型”）
 *
 * @param productCode              产品编码
 * @param metricDefinitions
 * @param derivedMetricDefinitions
 */
public record TelemetrySchema(
        String productCode,
        List<TelemetryMetricDefinition> metricDefinitions,
        List<DerivedMetricDefinition> derivedMetricDefinitions) {

    public TelemetrySchema {
        Objects.requireNonNull(productCode, "productCode must not be null");
        Objects.requireNonNull(metricDefinitions, "metricDefinitions must not be null");
        Objects.requireNonNull(derivedMetricDefinitions, "derivedMetricDefinitions must not be null");
        if (productCode.isBlank()) {
            throw new BaseDomainException("productCode must not be blank");
        }
        metricDefinitions = List.copyOf(metricDefinitions);
        derivedMetricDefinitions = List.copyOf(derivedMetricDefinitions);
    }

    /**
     * 验证遥测数据是否符合定义
     *
     * @param metrics
     */
    public void validate(TelemetryMetrics metrics) {
        Objects.requireNonNull(metrics, "metrics must not be null");
        // 基础指标和派生指标是否在metrics范围内
        boolean unsupportedMetricExists = metrics.values().keySet().stream()
                .anyMatch(metricName -> metricDefinitions.stream()
                        .map(TelemetryMetricDefinition::metricName)
                        .noneMatch(metricName::equals)
                        && derivedMetricDefinitions.stream()
                        .map(DerivedMetricDefinition::metricName)
                        .noneMatch(metricName::equals));
        if (unsupportedMetricExists) {
            throw new BaseDomainException("telemetry metrics do not match telemetry schema. productCode=" + productCode);
        }
        // 验证基础指标值是否满足要求
        for (TelemetryMetricDefinition metricDefinition : metricDefinitions) {
            metricDefinition.validate(metrics.valueOf(metricDefinition.metricName()), productCode);
        }
        // 验证派生指标值是否满足要求
        for (DerivedMetricDefinition derivedMetricDefinition : derivedMetricDefinitions) {
            if (derivedMetricDefinition.required() && metrics.valueOf(derivedMetricDefinition.metricName()) == null) {
                throw new BaseDomainException("required derived telemetry metric is missing. productCode="
                        + productCode + ", metric=" + derivedMetricDefinition.metricName().value());
            }
        }
    }
}
