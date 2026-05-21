package com.example.iotalarmcopilot.device.infrastructure.model;

import com.example.iotalarmcopilot.device.domain.model.*;
import com.example.iotalarmcopilot.device.domain.repository.ProductModelRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 产品模型仓储（模拟）
 */
@Repository
public class InMemoryProductModelRepository implements ProductModelRepository {

    private final Map<String, ProductModel> models = Map.of(
            "prod-sensor", new ProductModel(
                    1L,
                    new ProductCode("prod-sensor"),
                    "Environment Sensor",
                    List.of(
                            new CapabilityCode("temperature"),
                            new CapabilityCode("humidity"),
                            new CapabilityCode("runningStatus"),
                            new CapabilityCode("heatIndex"),
                            new CapabilityCode("sampleInterval")),
                    new TelemetrySchema(
                            List.of(
                                    new TelemetryMetricDefinition(
                                            new CapabilityCode("temperature"),
                                            "/temperature",
                                            null,
                                            TelemetryTransformType.IDENTITY,
                                            null,
                                            null,
                                            true,
                                            "C",
                                            BigDecimal.valueOf(-40),
                                            BigDecimal.valueOf(125)),
                                    new TelemetryMetricDefinition(
                                            new CapabilityCode("humidity"),
                                            "/humidity",
                                            null,
                                            TelemetryTransformType.IDENTITY,
                                            null,
                                            null,
                                            false,
                                            "%",
                                            BigDecimal.ZERO,
                                            BigDecimal.valueOf(100)),
                                    new TelemetryMetricDefinition(
                                            new CapabilityCode("runningStatus"),
                                            "/status",
                                            new BinaryStateMapping(
                                                    "ON",
                                                    "OFF",
                                                    BigDecimal.ONE,
                                                    BigDecimal.ZERO),
                                            TelemetryTransformType.IDENTITY,
                                            null,
                                            null,
                                            false,
                                            "state",
                                            BigDecimal.ZERO,
                                            BigDecimal.ONE)),
                            List.of(
                                    new DerivedMetricDefinition(
                                            new CapabilityCode("heatIndex"),
                                            List.of(
                                                    new CapabilityCode("temperature"),
                                                    new CapabilityCode("humidity")),
                                            "temperature + (humidity * 0.1)",
                                            false,
                                            "C"))),
                    new ShadowSchema(
                            List.of("temperature", "humidity", "runningStatus", "heatIndex"),
                            List.of("sampleInterval")),
                    new ThingModel(
                            new ThingModelVersion(1),
                            List.of(
                                    new ThingPropertyDefinition(
                                            new CapabilityCode("temperature"),
                                            ThingPropertySource.TELEMETRY,
                                            ThingPropertyAccessMode.READ_ONLY,
                                            ThingDataType.DECIMAL,
                                            "C"),
                                    new ThingPropertyDefinition(
                                            new CapabilityCode("humidity"),
                                            ThingPropertySource.TELEMETRY,
                                            ThingPropertyAccessMode.READ_ONLY,
                                            ThingDataType.DECIMAL,
                                            "%"),
                                    new ThingPropertyDefinition(
                                            new CapabilityCode("runningStatus"),
                                            ThingPropertySource.TELEMETRY,
                                            ThingPropertyAccessMode.READ_ONLY,
                                            ThingDataType.BOOLEAN,
                                            "state"),
                                    new ThingPropertyDefinition(
                                            new CapabilityCode("heatIndex"),
                                            ThingPropertySource.DERIVED,
                                            ThingPropertyAccessMode.READ_ONLY,
                                            ThingDataType.DECIMAL,
                                            "C"),
                                    new ThingPropertyDefinition(
                                            new CapabilityCode("sampleInterval"),
                                            ThingPropertySource.SHADOW_DESIRED,
                                            ThingPropertyAccessMode.READ_WRITE,
                                            ThingDataType.INTEGER,
                                            "second")),
                            List.of(
                                    new ThingEventDefinition(
                                            "overheat",
                                            "OverheatDetected",
                                            List.of(
                                                    new CapabilityCode("temperature"),
                                                    new CapabilityCode("heatIndex"))),
                                    new ThingEventDefinition(
                                            "deviceStarted",
                                            "DeviceStarted",
                                            List.of(new CapabilityCode("runningStatus")))),
                            List.of(
                                    new ThingServiceDefinition(
                                            "setSampleInterval",
                                            "SetSampleInterval",
                                            List.of(new CapabilityCode("sampleInterval"))),
                                    new ThingServiceDefinition(
                                            "reboot",
                                            "Reboot",
                                            List.of()))),
                    Instant.parse("2026-05-18T00:00:00Z"),
                    Instant.parse("2026-05-18T00:00:00Z")));

    @Override
    public Optional<ProductModel> findByProductCode(ProductCode productCode) {
        return Optional.ofNullable(models.get(productCode.value()));
    }

    @Override
    public List<ProductModel> findAll() {
        return models.values().stream().toList();
    }
}
