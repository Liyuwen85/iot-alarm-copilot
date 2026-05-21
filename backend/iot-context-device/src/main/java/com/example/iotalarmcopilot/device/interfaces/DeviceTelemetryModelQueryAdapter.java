package com.example.iotalarmcopilot.device.interfaces;

import com.example.iotalarmcopilot.contract.device.*;
import com.example.iotalarmcopilot.device.domain.model.Device;
import com.example.iotalarmcopilot.device.domain.model.DeviceCode;
import com.example.iotalarmcopilot.device.domain.repository.DeviceRepository;
import com.example.iotalarmcopilot.device.domain.repository.ProductModelRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 设备遥测模型查询适配器(对外)
 */
@Component
public class DeviceTelemetryModelQueryAdapter implements DeviceTelemetryModelQueryPort {

    private final DeviceRepository deviceRepository;
    private final ProductModelRepository productModelRepository;

    public DeviceTelemetryModelQueryAdapter(
            DeviceRepository deviceRepository,
            ProductModelRepository productModelRepository) {
        this.deviceRepository = deviceRepository;
        this.productModelRepository = productModelRepository;
    }

    @Override
    public Optional<DeviceTelemetryModel> findTelemetryModel(String deviceCode) {
        return deviceRepository.findByDeviceCode(new DeviceCode(deviceCode))
                .flatMap(this::toTelemetryModel);
    }

    /**
     * 转换为设备遥测模型
     *
     * @param device 设备
     * @return 设备遥测模型
     */
    private Optional<DeviceTelemetryModel> toTelemetryModel(Device device) {
        return productModelRepository.findByProductCode(device.productCode())
                .map(productModel -> new DeviceTelemetryModel(
                        device.deviceCode().value(),
                        productModel.productCode().value(),
                        productModel.telemetrySchema().metricDefinitions().stream()
                                .map(metric -> new TelemetryMetricContract(
                                        metric.capabilityCode().value(),
                                        metric.sourcePath(),
                                        metric.binaryStateMapping() == null
                                                ? null
                                                : new BinaryStateMappingContract(
                                                metric.binaryStateMapping().activeLiteral(),
                                                metric.binaryStateMapping().inactiveLiteral(),
                                                metric.binaryStateMapping().activeValue(),
                                                metric.binaryStateMapping().inactiveValue()),
                                        TelemetryTransformType.valueOf(metric.transformType().name()),
                                        metric.factor(),
                                        metric.offset(),
                                        metric.required(),
                                        metric.unit(),
                                        metric.minValue(),
                                        metric.maxValue()))
                                .toList(),
                        productModel.telemetrySchema().derivedMetricDefinitions().stream()
                                .map(metric -> new DerivedMetricContract(
                                        metric.capabilityCode().value(),
                                        metric.sourceMetrics().stream().map(sourceMetric -> sourceMetric.value()).toList(),
                                        metric.expression(),
                                        metric.required(),
                                        metric.unit()))
                                .toList()));
    }
}
