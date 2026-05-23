package com.example.iotalarmcopilot.contract.device;

import java.util.List;
import java.util.Objects;

/**
 * 设备遥测模型
 */
public record DeviceTelemetryModel(
        String deviceCode,
        String productCode,
        List<TelemetryMetricContract> metricContracts,
        List<DerivedMetricContract> derivedMetricContracts) {

    public DeviceTelemetryModel {
        Objects.requireNonNull(deviceCode, "deviceCode must not be null");
        Objects.requireNonNull(productCode, "productCode must not be null");
        Objects.requireNonNull(metricContracts, "metricContracts must not be null");
        Objects.requireNonNull(derivedMetricContracts, "derivedMetricContracts must not be null");
        if (deviceCode.isBlank()) {
            throw new IllegalArgumentException("deviceCode must not be blank");
        }
        if (productCode.isBlank()) {
            throw new IllegalArgumentException("productCode must not be blank");
        }
        metricContracts = List.copyOf(metricContracts);
        derivedMetricContracts = List.copyOf(derivedMetricContracts);
    }
}
