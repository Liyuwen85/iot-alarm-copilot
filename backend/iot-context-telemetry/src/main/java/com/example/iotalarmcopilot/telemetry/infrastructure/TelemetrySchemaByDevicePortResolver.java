package com.example.iotalarmcopilot.telemetry.infrastructure;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryModel;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryModelQueryPort;
import com.example.iotalarmcopilot.telemetry.application.TelemetrySchemaResolver;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryMetricDefinition;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySchema;
import org.springframework.stereotype.Component;

/**
 * 遥测schema解析器
 */
@Component
public class TelemetrySchemaByDevicePortResolver implements TelemetrySchemaResolver {

    private final DeviceTelemetryModelQueryPort deviceTelemetryModelQueryPort;

    public TelemetrySchemaByDevicePortResolver(DeviceTelemetryModelQueryPort deviceTelemetryModelQueryPort) {
        this.deviceTelemetryModelQueryPort = deviceTelemetryModelQueryPort;
    }

    @Override
    public TelemetrySchema resolveByDeviceId(String deviceId) {
        // 返回设备遥测模型
        DeviceTelemetryModel model = deviceTelemetryModelQueryPort.findTelemetryModel(deviceId)
                .orElseThrow(() -> new BaseDomainException("Device telemetry model not found. deviceCode=" + deviceId));
        return new TelemetrySchema(
                model.productCode(),
                model.metricContracts().stream()
                        .map(metric -> new TelemetryMetricDefinition(
                                new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName(metric.metricCode()),
                                metric.required(),
                                metric.unit(),
                                metric.minValue(),
                                metric.maxValue()))
                        .toList(),
                model.derivedMetricContracts().stream()
                        .map(metric -> new com.example.iotalarmcopilot.telemetry.domain.DerivedMetricDefinition(
                                new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName(metric.metricCode()),
                                metric.sourceMetrics().stream()
                                        .map(com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName::new)
                                        .toList(),
                                metric.expression(),
                                metric.required(),
                                metric.unit()))
                        .toList());
    }
}
