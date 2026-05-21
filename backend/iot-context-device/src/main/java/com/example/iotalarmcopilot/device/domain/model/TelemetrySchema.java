package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 遥测模型。
 *
 * @param metricDefinitions        基础指标
 * @param derivedMetricDefinitions 可计算得到的派生指标
 */
public record TelemetrySchema(
        List<TelemetryMetricDefinition> metricDefinitions,
        List<DerivedMetricDefinition> derivedMetricDefinitions) {

    public TelemetrySchema {
        Objects.requireNonNull(metricDefinitions, "metricDefinitions must not be null");
        Objects.requireNonNull(derivedMetricDefinitions, "derivedMetricDefinitions must not be null");
        metricDefinitions = List.copyOf(metricDefinitions);
        derivedMetricDefinitions = List.copyOf(derivedMetricDefinitions);

        boolean containsNullDefinition = metricDefinitions.stream().anyMatch(Objects::isNull);
        if (containsNullDefinition) {
            throw new BaseDomainException("metricDefinitions must not contain null definition");
        }
        boolean containsNullDerivedDefinition = derivedMetricDefinitions.stream().anyMatch(Objects::isNull);
        if (containsNullDerivedDefinition) {
            throw new BaseDomainException("derivedMetricDefinitions must not contain null definition");
        }

        // 同一个schema中不能有重复的能力代码
        Set<CapabilityCode> metricCodes = new HashSet<>();
        for (TelemetryMetricDefinition metricDefinition : metricDefinitions) {
            if (!metricCodes.add(metricDefinition.capabilityCode())) {
                throw new BaseDomainException("duplicate telemetry metric definition: "
                        + metricDefinition.capabilityCode().value());
            }
        }

        Set<CapabilityCode> derivedMetricCodes = new HashSet<>();
        for (DerivedMetricDefinition derivedMetricDefinition : derivedMetricDefinitions) {
            if (!derivedMetricCodes.add(derivedMetricDefinition.capabilityCode())) {
                throw new BaseDomainException("duplicate derived telemetry metric definition: "
                        + derivedMetricDefinition.capabilityCode().value());
            }
            if (metricCodes.contains(derivedMetricDefinition.capabilityCode())) {
                throw new BaseDomainException("telemetry metric and derived metric must not share code: "
                        + derivedMetricDefinition.capabilityCode().value());
            }
            boolean containsUndefinedSource = derivedMetricDefinition.sourceMetrics().stream()
                    .anyMatch(sourceMetric -> !metricCodes.contains(sourceMetric));
            if (containsUndefinedSource) {
                throw new BaseDomainException("derived metric source must reference base telemetry metric only: "
                        + derivedMetricDefinition.capabilityCode().value());
            }
        }
    }

    /**
     * 是否支持指定能力
     *
     * @param capabilityCode 能力代码
     * @return 是否支持
     */
    public boolean supports(CapabilityCode capabilityCode) {
        Objects.requireNonNull(capabilityCode, "capabilityCode must not be null");
        return metricDefinitions.stream()
                .map(TelemetryMetricDefinition::capabilityCode)
                .anyMatch(capabilityCode::equals)
                || derivedMetricDefinitions.stream()
                .map(DerivedMetricDefinition::capabilityCode)
                .anyMatch(capabilityCode::equals);
    }
}
