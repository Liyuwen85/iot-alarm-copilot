package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.telemetry.domain.DerivedMetricDefinition;

import java.util.List;

/**
 * 派生指标计算器（仅单设备）
 */
public interface TelemetryDerivedMetricCalculator {

    TelemetryMetrics apply(TelemetryMetrics baseMetrics, List<DerivedMetricDefinition> derivedMetricDefinitions);
}
