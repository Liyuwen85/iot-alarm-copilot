package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductModelTest {

    @Test
    void should_validate_shadow_document_against_schema() {
        ProductModel productModel = new ProductModel(
                1L,
                new ProductCode("prod-sensor"),
                "Environment Sensor",
                List.of(new CapabilityCode("temperature")),
                new TelemetrySchema(
                        List.of(new TelemetryMetricDefinition(
                                new CapabilityCode("temperature"),
                                "/temperature",
                                null,
                                TelemetryTransformType.IDENTITY,
                                null,
                                null,
                                true,
                                "C",
                                null,
                                null)),
                        List.of()),
                new ShadowSchema(List.of("\"temperature\""), List.of("\"sampleInterval\"")),
                ThingModel.empty(),
                Instant.parse("2026-05-18T00:00:00Z"),
                Instant.parse("2026-05-18T00:00:00Z"));

        assertDoesNotThrow(() ->
                productModel.validateShadowDocument("{\"reported\":{\"temperature\":36.5},\"desired\":{\"sampleInterval\":10}}"));

        assertThrows(BaseDomainException.class, () ->
                productModel.validateShadowDocument("{\"reported\":{\"temperature\":36.5}}"));
    }

    @Test
    void should_support_declared_capability() {
        ProductModel productModel = new ProductModel(
                1L,
                new ProductCode("prod-sensor"),
                "Environment Sensor",
                List.of(new CapabilityCode("temperature")),
                new TelemetrySchema(
                        List.of(new TelemetryMetricDefinition(
                                new CapabilityCode("temperature"),
                                "/temperature",
                                null,
                                TelemetryTransformType.IDENTITY,
                                null,
                                null,
                                true,
                                "C",
                                null,
                                null)),
                        List.of()),
                new ShadowSchema(List.of("\"temperature\""), List.of("\"sampleInterval\"")),
                ThingModel.empty(),
                Instant.parse("2026-05-18T00:00:00Z"),
                Instant.parse("2026-05-18T00:00:00Z"));

        assertTrue(productModel.supports(new CapabilityCode("temperature")));
    }

    @Test
    void should_reject_binary_state_mapping_with_non_identity_transform() {
        assertThrows(BaseDomainException.class, () -> new TelemetryMetricDefinition(
                new CapabilityCode("runningStatus"),
                "/runningStatus",
                new BinaryStateMapping("ON", "OFF", java.math.BigDecimal.ONE, java.math.BigDecimal.ZERO),
                TelemetryTransformType.SCALE,
                java.math.BigDecimal.TEN,
                null,
                false,
                "state",
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ONE));
    }

    @Test
    void should_reject_derived_metric_that_depends_on_non_base_metric() {
        assertThrows(BaseDomainException.class, () -> new TelemetrySchema(
                List.of(new TelemetryMetricDefinition(
                        new CapabilityCode("temperature"),
                        "/temperature",
                        null,
                        TelemetryTransformType.IDENTITY,
                        null,
                        null,
                        true,
                        "C",
                        null,
                        null)),
                List.of(new DerivedMetricDefinition(
                        new CapabilityCode("heatIndex"),
                        List.of(new CapabilityCode("temperature"), new CapabilityCode("humidity")),
                        "temperature + (humidity * 0.1)",
                        false,
                        "C"))));
    }

    @Test
    void should_reject_product_model_when_schema_metric_not_declared_in_capabilities() {
        assertThrows(BaseDomainException.class, () -> new ProductModel(
                1L,
                new ProductCode("prod-sensor"),
                "Environment Sensor",
                List.of(new CapabilityCode("humidity")),
                new TelemetrySchema(
                        List.of(new TelemetryMetricDefinition(
                                new CapabilityCode("temperature"),
                                "/temperature",
                                null,
                                TelemetryTransformType.IDENTITY,
                                null,
                                null,
                                true,
                                "C",
                                null,
                                null)),
                        List.of()),
                new ShadowSchema(List.of("\"temperature\""), List.of("\"sampleInterval\"")),
                ThingModel.empty(),
                Instant.parse("2026-05-18T00:00:00Z"),
                Instant.parse("2026-05-18T00:00:00Z")));
    }

    @Test
    void should_accept_minimal_thing_model_when_property_event_service_are_consistent() {
        assertDoesNotThrow(() -> new ProductModel(
                1L,
                new ProductCode("prod-sensor"),
                "Environment Sensor",
                List.of(
                        new CapabilityCode("temperature"),
                        new CapabilityCode("sampleInterval"),
                        new CapabilityCode("highTempFlag")),
                new TelemetrySchema(
                        List.of(new TelemetryMetricDefinition(
                                new CapabilityCode("temperature"),
                                "/temperature",
                                null,
                                TelemetryTransformType.IDENTITY,
                                null,
                                null,
                                true,
                                "C",
                                null,
                                null)),
                        List.of()),
                new ShadowSchema(List.of("temperature"), List.of("sampleInterval")),
                new ThingModel(
                        new ThingModelVersion(2),
                        List.of(
                                new ThingPropertyDefinition(
                                        new CapabilityCode("temperature"),
                                        ThingPropertySource.TELEMETRY,
                                        ThingPropertyAccessMode.READ_ONLY,
                                        ThingDataType.DECIMAL,
                                        "C"),
                                new ThingPropertyDefinition(
                                        new CapabilityCode("sampleInterval"),
                                        ThingPropertySource.SHADOW_DESIRED,
                                        ThingPropertyAccessMode.READ_WRITE,
                                        ThingDataType.INTEGER,
                                        "second")),
                        List.of(new ThingEventDefinition(
                                "highTemp",
                                "HighTemperatureDetected",
                                List.of(new CapabilityCode("temperature"), new CapabilityCode("highTempFlag")))),
                        List.of(new ThingServiceDefinition(
                                "setSampleInterval",
                                "SetSampleInterval",
                                List.of(new CapabilityCode("sampleInterval"))))),
                Instant.parse("2026-05-18T00:00:00Z"),
                Instant.parse("2026-05-18T00:00:00Z")));
    }

    @Test
    void should_reject_thing_property_when_source_not_match_product_model() {
        assertThrows(BaseDomainException.class, () -> new ProductModel(
                1L,
                new ProductCode("prod-sensor"),
                "Environment Sensor",
                List.of(new CapabilityCode("sampleInterval")),
                new TelemetrySchema(List.of(), List.of()),
                new ShadowSchema(List.of(), List.of("sampleInterval")),
                new ThingModel(
                        new ThingModelVersion(1),
                        List.of(new ThingPropertyDefinition(
                                new CapabilityCode("sampleInterval"),
                                ThingPropertySource.TELEMETRY,
                                ThingPropertyAccessMode.READ_ONLY,
                                ThingDataType.INTEGER,
                                "second")),
                        List.of(),
                        List.of()),
                Instant.parse("2026-05-18T00:00:00Z"),
                Instant.parse("2026-05-18T00:00:00Z")));
    }

    @Test
    void should_reject_thing_model_when_version_is_not_positive() {
        assertThrows(BaseDomainException.class, () -> new ThingModel(
                new ThingModelVersion(0),
                List.of(),
                List.of(),
                List.of()));
    }
}
