package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.access.application.port.TelemetryMessageParser;
import com.example.iotalarmcopilot.access.infrastructure.parser.JacksonTelemetryMessageParser;
import com.example.iotalarmcopilot.contract.device.BinaryStateMappingContract;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryIngestionPort;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryModel;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryModelQueryPort;
import com.example.iotalarmcopilot.contract.device.TelemetryMetricContract;
import com.example.iotalarmcopilot.contract.device.TelemetryTransformType;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.telemetry.application.TelemetryHotDataPort;
import com.example.iotalarmcopilot.telemetry.application.TelemetryIngestApplicationService;
import com.example.iotalarmcopilot.telemetry.application.TelemetrySchemaResolver;
import com.example.iotalarmcopilot.telemetry.domain.DeviceId;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.application.port.TelemetryEventIdGenerator;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryMetricDefinition;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySchema;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshot;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshotRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelemetryAccessApplicationServiceTest {

    private final TelemetryMessageParser telemetryMessageParser =
            new JacksonTelemetryMessageParser(new ObjectMapper());

    @Test
    void should_ingest_from_kafka_with_same_access_pipeline() {
        AtomicReference<TelemetryEvent> saved = new AtomicReference<>();
        AtomicReference<String> validatedDevice = new AtomicReference<>();
        TelemetryEventIdGenerator idGenerator = (deviceId, reportedAt, rawJson) -> 1L;
        DeviceTelemetryIngestionPort ingestionPort = validatedDevice::set;
        DeviceTelemetryModelQueryPort deviceTelemetryModelQueryPort = deviceCode -> Optional.of(
                new DeviceTelemetryModel(
                        deviceCode,
                        "prod-sensor",
                        List.of(new TelemetryMetricContract(
                                "temperature",
                                "/properties/tempC",
                                null,
                                TelemetryTransformType.IDENTITY,
                                null,
                                null,
                                true,
                                "C",
                                null,
                                null)),
                        List.of()));
        TelemetryHotDataPort hotDataPort = new TelemetryHotDataPort() {
            @Override
            public void append(TelemetryEvent event) {
                saved.set(event);
            }

            @Override
            public java.util.List<com.example.iotalarmcopilot.telemetry.application.TelemetryEventVO> recent(int limit) {
                return java.util.List.of();
            }
        };
        TelemetrySnapshotRepository snapshotRepository = new TelemetrySnapshotRepository() {
            @Override
            public void save(TelemetrySnapshot snapshot) {
            }

            @Override
            public Optional<TelemetrySnapshot> findByDeviceId(DeviceId deviceId) {
                return Optional.empty();
            }
        };
        ApplicationEventPublisher publisher = event -> {
        };
        TelemetrySchemaResolver telemetrySchemaResolver = deviceId -> new TelemetrySchema(
                "prod-sensor",
                List.of(new TelemetryMetricDefinition(
                        new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("temperature"),
                        true,
                        "C",
                        null,
                        null)),
                List.of());
        TelemetryIngestApplicationService telemetryIngestApplicationService =
                new TelemetryIngestApplicationService(
                        idGenerator,
                        telemetrySchemaResolver,
                        (baseMetrics, derivedMetricDefinitions) -> baseMetrics,
                        hotDataPort,
                        snapshotRepository,
                        publisher);
        TelemetryAccessApplicationService service =
                new TelemetryAccessApplicationService(
                        telemetryMessageParser,
                        ingestionPort,
                        deviceTelemetryModelQueryPort,
                        telemetryIngestApplicationService);

        service.ingestKafkaTelemetry(
                "iot/dev-01/telemetry",
                "{\"deviceId\":\"dev-01\",\"properties\":{\"tempC\":81.5},\"reportedAt\":\"2026-05-14T10:00:00Z\"}");

        assertEquals("dev-01", validatedDevice.get());
        assertEquals("dev-01", saved.get().deviceId().value());
        assertEquals("81.5", saved.get().temperature().toPlainString());
    }

    @Test
    void should_ingest_from_mqtt_with_same_access_pipeline() {
        AtomicReference<TelemetryEvent> saved = new AtomicReference<>();
        AtomicReference<String> validatedDevice = new AtomicReference<>();
        TelemetryEventIdGenerator idGenerator = (deviceId, reportedAt, rawJson) -> 1L;
        DeviceTelemetryIngestionPort ingestionPort = validatedDevice::set;
        DeviceTelemetryModelQueryPort deviceTelemetryModelQueryPort = deviceCode -> Optional.of(
                new DeviceTelemetryModel(
                        deviceCode,
                        "prod-sensor",
                        List.of(new TelemetryMetricContract(
                                "humidity",
                                "/metrics/humidity",
                                null,
                                TelemetryTransformType.IDENTITY,
                                null,
                                null,
                                true,
                                "%",
                                null,
                                null)),
                        List.of()));
        TelemetryHotDataPort hotDataPort = new TelemetryHotDataPort() {
            @Override
            public void append(TelemetryEvent event) {
                saved.set(event);
            }

            @Override
            public java.util.List<com.example.iotalarmcopilot.telemetry.application.TelemetryEventVO> recent(int limit) {
                return java.util.List.of();
            }
        };
        TelemetrySnapshotRepository snapshotRepository = new TelemetrySnapshotRepository() {
            @Override
            public void save(TelemetrySnapshot snapshot) {
            }

            @Override
            public Optional<TelemetrySnapshot> findByDeviceId(DeviceId deviceId) {
                return Optional.empty();
            }
        };
        ApplicationEventPublisher publisher = event -> {
        };
        TelemetrySchemaResolver telemetrySchemaResolver = deviceId -> new TelemetrySchema(
                "prod-sensor",
                List.of(new TelemetryMetricDefinition(
                        new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("humidity"),
                        true,
                        "%",
                        null,
                        null)),
                List.of());
        TelemetryIngestApplicationService telemetryIngestApplicationService =
                new TelemetryIngestApplicationService(
                        idGenerator,
                        telemetrySchemaResolver,
                        (baseMetrics, derivedMetricDefinitions) -> baseMetrics,
                        hotDataPort,
                        snapshotRepository,
                        publisher);
        TelemetryAccessApplicationService service =
                new TelemetryAccessApplicationService(
                        telemetryMessageParser,
                        ingestionPort,
                        deviceTelemetryModelQueryPort,
                        telemetryIngestApplicationService);

        service.ingestMqttTelemetry(
                "iot/dev-02/telemetry",
                "{\"deviceId\":\"dev-02\",\"metrics\":{\"humidity\":65.2},\"reportedAt\":\"2026-05-14T10:05:00Z\"}");

        assertEquals("dev-02", validatedDevice.get());
        assertEquals("dev-02", saved.get().deviceId().value());
        assertEquals("65.2", saved.get().humidity().toPlainString());
    }

    @Test
    void should_ingest_binary_state_metric_from_mqtt() {
        AtomicReference<TelemetryEvent> saved = new AtomicReference<>();
        AtomicReference<String> validatedDevice = new AtomicReference<>();
        TelemetryEventIdGenerator idGenerator = (deviceId, reportedAt, rawJson) -> 1L;
        DeviceTelemetryIngestionPort ingestionPort = validatedDevice::set;
        DeviceTelemetryModelQueryPort deviceTelemetryModelQueryPort = deviceCode -> Optional.of(
                new DeviceTelemetryModel(
                        deviceCode,
                        "prod-sensor",
                        List.of(new TelemetryMetricContract(
                                "runningStatus",
                                "/properties/status",
                                new BinaryStateMappingContract(
                                        "ON",
                                        "OFF",
                                        java.math.BigDecimal.ONE,
                                        java.math.BigDecimal.ZERO),
                                TelemetryTransformType.IDENTITY,
                                null,
                                null,
                                false,
                                "state",
                                java.math.BigDecimal.ZERO,
                                java.math.BigDecimal.ONE)),
                        List.of()));
        TelemetryHotDataPort hotDataPort = new TelemetryHotDataPort() {
            @Override
            public void append(TelemetryEvent event) {
                saved.set(event);
            }

            @Override
            public java.util.List<com.example.iotalarmcopilot.telemetry.application.TelemetryEventVO> recent(int limit) {
                return java.util.List.of();
            }
        };
        TelemetrySnapshotRepository snapshotRepository = new TelemetrySnapshotRepository() {
            @Override
            public void save(TelemetrySnapshot snapshot) {
            }

            @Override
            public Optional<TelemetrySnapshot> findByDeviceId(DeviceId deviceId) {
                return Optional.empty();
            }
        };
        ApplicationEventPublisher publisher = event -> {
        };
        TelemetrySchemaResolver telemetrySchemaResolver = deviceId -> new TelemetrySchema(
                "prod-sensor",
                List.of(new TelemetryMetricDefinition(
                        new TelemetryMetricName("runningStatus"),
                        false,
                        "state",
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ONE)),
                List.of());
        TelemetryIngestApplicationService telemetryIngestApplicationService =
                new TelemetryIngestApplicationService(
                        idGenerator,
                        telemetrySchemaResolver,
                        (baseMetrics, derivedMetricDefinitions) -> baseMetrics,
                        hotDataPort,
                        snapshotRepository,
                        publisher);
        TelemetryAccessApplicationService service =
                new TelemetryAccessApplicationService(
                        telemetryMessageParser,
                        ingestionPort,
                        deviceTelemetryModelQueryPort,
                        telemetryIngestApplicationService);

        service.ingestMqttTelemetry(
                "iot/dev-03/telemetry",
                "{\"deviceId\":\"dev-03\",\"properties\":{\"status\":\"ON\"},\"reportedAt\":\"2026-05-14T10:05:00Z\"}");

        assertEquals("dev-03", validatedDevice.get());
        assertEquals("dev-03", saved.get().deviceId().value());
        assertEquals("1", saved.get().metrics().valueOf(new TelemetryMetricName("runningStatus")).toPlainString());
    }
}
