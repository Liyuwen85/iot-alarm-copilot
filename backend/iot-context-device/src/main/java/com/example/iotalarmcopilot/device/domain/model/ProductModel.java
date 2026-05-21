package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 产品模型聚合根。负责产品主数据与能力定义。
 *
 * @param id
 * @param productCode
 * @param productName
 * @param capabilities    能力列表
 * @param telemetrySchema 关联的遥测指标模型
 * @param shadowSchema    关联的设备影子模型
 * @param thingModel      关联的物模型
 * @param createdAt
 * @param updatedAt
 */
public record ProductModel(
        Long id,
        ProductCode productCode,
        String productName,
        List<CapabilityCode> capabilities,
        TelemetrySchema telemetrySchema,
        ShadowSchema shadowSchema,
        ThingModel thingModel,
        Instant createdAt,
        Instant updatedAt) {

    public ProductModel {
        Objects.requireNonNull(productCode, "productCode must not be null");
        Objects.requireNonNull(productName, "productName must not be null");
        Objects.requireNonNull(capabilities, "capabilities must not be null");
        Objects.requireNonNull(telemetrySchema, "telemetrySchema must not be null");
        Objects.requireNonNull(shadowSchema, "shadowSchema must not be null");
        Objects.requireNonNull(thingModel, "thingModel must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        capabilities = List.copyOf(capabilities);
        if (productName.isBlank()) {
            throw new BaseDomainException("productName must not be blank");
        }
        if (capabilities.stream().anyMatch(Objects::isNull)) {
            throw new BaseDomainException("capabilities must not contain null capability");
        }

        Set<CapabilityCode> declaredCapabilities = new HashSet<>(capabilities);
        if (declaredCapabilities.size() != capabilities.size()) {
            throw new BaseDomainException("capabilities must not contain duplicate capability");
        }

        boolean containsUndeclaredMetric = telemetrySchema.metricDefinitions().stream()
                .map(TelemetryMetricDefinition::capabilityCode)
                .anyMatch(metricCode -> !declaredCapabilities.contains(metricCode));
        if (containsUndeclaredMetric) {
            throw new BaseDomainException("telemetry metric must be declared in product capabilities");
        }

        boolean containsUndeclaredDerivedMetric = telemetrySchema.derivedMetricDefinitions().stream()
                .map(DerivedMetricDefinition::capabilityCode)
                .anyMatch(metricCode -> !declaredCapabilities.contains(metricCode));
        if (containsUndeclaredDerivedMetric) {
            throw new BaseDomainException("derived telemetry metric must be declared in product capabilities");
        }

        validateThingModel(thingModel, declaredCapabilities, telemetrySchema, shadowSchema);
    }

    public void validateShadowDocument(String shadowDocument) {
        shadowSchema.validateDocument(shadowDocument);
    }

    public boolean supports(CapabilityCode capabilityCode) {
        Objects.requireNonNull(capabilityCode, "capabilityCode must not be null");
        return capabilities.contains(capabilityCode);
    }

    private static void validateThingModel(
            ThingModel thingModel,
            Set<CapabilityCode> declaredCapabilities,
            TelemetrySchema telemetrySchema,
            ShadowSchema shadowSchema) {
        for (ThingPropertyDefinition property : thingModel.properties()) {
            if (!declaredCapabilities.contains(property.capabilityCode())) {
                throw new BaseDomainException("thing property must be declared in product capabilities: "
                        + property.capabilityCode().value());
            }
            if (!matchesPropertySource(property, telemetrySchema, shadowSchema)) {
                throw new BaseDomainException("thing property source does not match product model: "
                        + property.capabilityCode().value());
            }
        }

        for (ThingEventDefinition event : thingModel.events()) {
            boolean containsUndeclaredOutput = event.outputCapabilities().stream()
                    .anyMatch(output -> !declaredCapabilities.contains(output));
            if (containsUndeclaredOutput) {
                throw new BaseDomainException("thing event output must be declared in product capabilities: "
                        + event.eventCode());
            }
        }

        for (ThingServiceDefinition service : thingModel.services()) {
            boolean containsUndeclaredInput = service.inputCapabilities().stream()
                    .anyMatch(input -> !declaredCapabilities.contains(input));
            if (containsUndeclaredInput) {
                throw new BaseDomainException("thing service input must be declared in product capabilities: "
                        + service.serviceCode());
            }
        }
    }

    private static boolean matchesPropertySource(
            ThingPropertyDefinition property,
            TelemetrySchema telemetrySchema,
            ShadowSchema shadowSchema) {
        return switch (property.source()) {
            case TELEMETRY -> property.accessMode() == ThingPropertyAccessMode.READ_ONLY
                    && telemetrySchema.metricDefinitions().stream()
                    .map(TelemetryMetricDefinition::capabilityCode)
                    .anyMatch(property.capabilityCode()::equals);
            case DERIVED -> property.accessMode() == ThingPropertyAccessMode.READ_ONLY
                    && telemetrySchema.derivedMetricDefinitions().stream()
                    .map(DerivedMetricDefinition::capabilityCode)
                    .anyMatch(property.capabilityCode()::equals);
            case SHADOW_REPORTED -> property.accessMode() == ThingPropertyAccessMode.READ_ONLY
                    && shadowSchema.supportsReportedField(property.capabilityCode().value());
            case SHADOW_DESIRED -> property.accessMode() != ThingPropertyAccessMode.READ_ONLY
                    && shadowSchema.supportsDesiredField(property.capabilityCode().value());
        };
    }
}
