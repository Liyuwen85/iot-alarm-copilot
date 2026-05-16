package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.access.domain.TelemetryMessageParser;
import com.example.iotalarmcopilot.access.infrastructure.parser.JacksonTelemetryMessageParser;
import com.example.iotalarmcopilot.telemetry.application.TelemetryHotDataPort;
import com.example.iotalarmcopilot.telemetry.application.TelemetryIngestApplicationService;
import com.example.iotalarmcopilot.telemetry.domain.DeviceId;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEventIdGenerator;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshot;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshotRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelemetryAccessApplicationServiceTest {

    private final TelemetryMessageParser telemetryMessageParser =
            new JacksonTelemetryMessageParser(new ObjectMapper());

    @Test
    void should_ingest_from_kafka_with_same_access_pipeline() {
        AtomicReference<TelemetryEvent> saved = new AtomicReference<>();
        TelemetryEventIdGenerator idGenerator = (deviceId, reportedAt, rawJson) -> 1L;
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
                // no-op
            }

            @Override
            public Optional<TelemetrySnapshot> findByDeviceId(DeviceId deviceId) {
                return Optional.empty();
            }
        };
        ApplicationEventPublisher publisher = event -> {
            // no-op
        };
        TelemetryIngestApplicationService telemetryIngestApplicationService =
                new TelemetryIngestApplicationService(idGenerator, hotDataPort, snapshotRepository, publisher);
        TelemetryAccessApplicationService service =
                new TelemetryAccessApplicationService(telemetryMessageParser, telemetryIngestApplicationService);

        service.ingestKafkaTelemetry(
                "iot/dev-01/telemetry",
                "{\"deviceId\":\"dev-01\",\"temperature\":81.5,\"reportedAt\":\"2026-05-14T10:00:00Z\"}");

        assertEquals("dev-01", saved.get().deviceId().value());
        assertEquals("81.5", saved.get().temperature().toPlainString());
    }

    @Test
    void should_ingest_from_mqtt_with_same_access_pipeline() {
        AtomicReference<TelemetryEvent> saved = new AtomicReference<>();
        TelemetryEventIdGenerator idGenerator = (deviceId, reportedAt, rawJson) -> 1L;
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
                // no-op
            }

            @Override
            public Optional<TelemetrySnapshot> findByDeviceId(DeviceId deviceId) {
                return Optional.empty();
            }
        };
        ApplicationEventPublisher publisher = event -> {
            // no-op
        };
        TelemetryIngestApplicationService telemetryIngestApplicationService =
                new TelemetryIngestApplicationService(idGenerator, hotDataPort, snapshotRepository, publisher);
        TelemetryAccessApplicationService service =
                new TelemetryAccessApplicationService(telemetryMessageParser, telemetryIngestApplicationService);

        service.ingestMqttTelemetry(
                "iot/dev-02/telemetry",
                "{\"deviceId\":\"dev-02\",\"humidity\":65.2,\"reportedAt\":\"2026-05-14T10:05:00Z\"}");

        assertEquals("dev-02", saved.get().deviceId().value());
        assertEquals("65.2", saved.get().humidity().toPlainString());
    }
}
