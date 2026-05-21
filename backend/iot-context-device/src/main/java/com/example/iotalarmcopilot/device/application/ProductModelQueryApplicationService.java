package com.example.iotalarmcopilot.device.application;

import com.example.iotalarmcopilot.device.domain.model.CapabilityCode;
import com.example.iotalarmcopilot.device.domain.model.ProductCode;
import com.example.iotalarmcopilot.device.domain.model.ProductModel;
import com.example.iotalarmcopilot.device.domain.repository.ProductModelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品模型查询应用服务
 */
@Service
public class ProductModelQueryApplicationService {

    private final ProductModelRepository productModelRepository;

    public ProductModelQueryApplicationService(ProductModelRepository productModelRepository) {
        this.productModelRepository = productModelRepository;
    }

    public ProductModelVO get(String productCode) {
        return productModelRepository.findByProductCode(new ProductCode(productCode))
                .map(this::toVO)
                .orElse(null);
    }

    public List<ProductModelVO> all() {
        return productModelRepository.findAll().stream()
                .map(this::toVO)
                .toList();
    }

    public ProductModelVO toVO(ProductModel productModel) {
        return new ProductModelVO(
                productModel.id(),
                productModel.productCode().value(),
                productModel.productName(),
                productModel.capabilities().stream().map(capability -> capability.value()).toList(),
                productModel.telemetrySchema().metricDefinitions().stream()
                        .map(metric -> new TelemetryMetricDefinitionVO(
                                metric.capabilityCode().value(),
                                metric.sourcePath(),
                                metric.binaryStateMapping() == null
                                        ? null
                                        : new BinaryStateMappingVO(
                                        metric.binaryStateMapping().activeLiteral(),
                                        metric.binaryStateMapping().inactiveLiteral(),
                                        metric.binaryStateMapping().activeValue(),
                                        metric.binaryStateMapping().inactiveValue()),
                                metric.transformType().name(),
                                metric.factor(),
                                metric.offset(),
                                metric.required(),
                                metric.unit(),
                                metric.minValue(),
                                metric.maxValue()))
                        .toList(),
                productModel.telemetrySchema().derivedMetricDefinitions().stream()
                        .map(metric -> new DerivedMetricDefinitionVO(
                                metric.capabilityCode().value(),
                                metric.sourceMetrics().stream().map(CapabilityCode::value).toList(),
                                metric.expression(),
                                metric.required(),
                                metric.unit()))
                        .toList(),
                productModel.thingModel().version().value(),
                productModel.thingModel().properties().stream()
                        .map(property -> new ThingPropertyDefinitionVO(
                                property.capabilityCode().value(),
                                property.source().name(),
                                property.accessMode().name(),
                                property.dataType().name(),
                                property.unit()))
                        .toList(),
                productModel.thingModel().events().stream()
                        .map(event -> new ThingEventDefinitionVO(
                                event.eventCode(),
                                event.eventName(),
                                event.outputCapabilities().stream().map(CapabilityCode::value).toList()))
                        .toList(),
                productModel.thingModel().services().stream()
                        .map(service -> new ThingServiceDefinitionVO(
                                service.serviceCode(),
                                service.serviceName(),
                                service.inputCapabilities().stream().map(CapabilityCode::value).toList()))
                        .toList(),
                productModel.shadowSchema().reportedFields(),
                productModel.shadowSchema().desiredFields(),
                productModel.createdAt(),
                productModel.updatedAt());
    }
}
