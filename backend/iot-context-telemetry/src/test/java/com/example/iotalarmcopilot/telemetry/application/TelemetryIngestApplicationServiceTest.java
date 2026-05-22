package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.event.TelemetryRecordedEvent;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.telemetry.domain.DeviceId;
import com.example.iotalarmcopilot.telemetry.domain.DerivedMetricDefinition;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.application.port.TelemetryEventIdGenerator;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryMetricDefinition;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySchema;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshot;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelemetryIngestApplicationServiceTest {

    @Test
    void should_record_telemetry_and_publish_event() {
        TelemetryEventIdGenerator idGenerator = mock(TelemetryEventIdGenerator.class);
        TelemetrySchemaResolver telemetrySchemaResolver = mock(TelemetrySchemaResolver.class);
        TelemetryHotDataPort hotDataPort = mock(TelemetryHotDataPort.class);
        TelemetrySnapshotRepository snapshotRepository = mock(TelemetrySnapshotRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TelemetryIngestApplicationService service = new TelemetryIngestApplicationService(
                idGenerator,
                telemetrySchemaResolver,
                (baseMetrics, derivedMetricDefinitions) -> baseMetrics,
                hotDataPort,
                snapshotRepository,
                publisher);
        Instant reportedAt = Instant.parse("2026-05-13T10:00:00Z");
        RecordTelemetryCommand command = new RecordTelemetryCommand(
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(36.5), null),
                reportedAt,
                "{\"temperature\":36.5}");
        TelemetrySchema telemetrySchema = new TelemetrySchema(
                "prod-sensor",
                List.of(new TelemetryMetricDefinition(
                        new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("temperature"),
                        true,
                        "C",
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(100))),
                List.of());

        when(idGenerator.nextId("dev-01", reportedAt, "{\"temperature\":36.5}")).thenReturn(11L);
        when(snapshotRepository.findByDeviceId(new DeviceId("dev-01"))).thenReturn(Optional.empty());
        when(telemetrySchemaResolver.resolveByDeviceId("dev-01")).thenReturn(telemetrySchema);

        TelemetryEvent result = service.record(command);

        assertEquals(11L, result.id());
        verify(hotDataPort).append(any(TelemetryEvent.class));
        verify(snapshotRepository).save(TelemetrySnapshot.capture(result));
        verify(publisher).publishEvent(new TelemetryRecordedEvent(
                11L,
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(36.5), null),
                reportedAt));
    }

    @Test
    void should_reject_telemetry_when_metric_not_supported_by_schema() {
        TelemetryEventIdGenerator idGenerator = mock(TelemetryEventIdGenerator.class);
        TelemetrySchemaResolver telemetrySchemaResolver = mock(TelemetrySchemaResolver.class);
        TelemetryHotDataPort hotDataPort = mock(TelemetryHotDataPort.class);
        TelemetrySnapshotRepository snapshotRepository = mock(TelemetrySnapshotRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TelemetryIngestApplicationService service = new TelemetryIngestApplicationService(
                idGenerator,
                telemetrySchemaResolver,
                (baseMetrics, derivedMetricDefinitions) -> baseMetrics,
                hotDataPort,
                snapshotRepository,
                publisher);
        Instant reportedAt = Instant.parse("2026-05-13T10:00:00Z");
        RecordTelemetryCommand command = new RecordTelemetryCommand(
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(36.5), BigDecimal.valueOf(50.2)),
                reportedAt,
                "{\"temperature\":36.5,\"humidity\":50.2}");
        TelemetrySchema telemetrySchema = new TelemetrySchema(
                "prod-sensor",
                List.of(new TelemetryMetricDefinition(
                        new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("temperature"),
                        true,
                        "C",
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(100))),
                List.of());

        when(telemetrySchemaResolver.resolveByDeviceId("dev-01")).thenReturn(telemetrySchema);

        assertThrows(BaseDomainException.class, () -> service.record(command));
    }

    @Test
    void should_reject_telemetry_when_required_metric_is_missing() {
        TelemetryEventIdGenerator idGenerator = mock(TelemetryEventIdGenerator.class);
        TelemetrySchemaResolver telemetrySchemaResolver = mock(TelemetrySchemaResolver.class);
        TelemetryHotDataPort hotDataPort = mock(TelemetryHotDataPort.class);
        TelemetrySnapshotRepository snapshotRepository = mock(TelemetrySnapshotRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TelemetryIngestApplicationService service = new TelemetryIngestApplicationService(
                idGenerator,
                telemetrySchemaResolver,
                (baseMetrics, derivedMetricDefinitions) -> baseMetrics,
                hotDataPort,
                snapshotRepository,
                publisher);
        Instant reportedAt = Instant.parse("2026-05-13T10:00:00Z");
        RecordTelemetryCommand command = new RecordTelemetryCommand(
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(null, BigDecimal.valueOf(50.2)),
                reportedAt,
                "{\"humidity\":50.2}");
        TelemetrySchema telemetrySchema = new TelemetrySchema(
                "prod-sensor",
                List.of(
                        new TelemetryMetricDefinition(
                                new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("temperature"),
                                true,
                                "C",
                                BigDecimal.ZERO,
                                BigDecimal.valueOf(100)),
                        new TelemetryMetricDefinition(
                                new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("humidity"),
                                false,
                                "%",
                                BigDecimal.ZERO,
                                BigDecimal.valueOf(100))),
                List.of());

        when(telemetrySchemaResolver.resolveByDeviceId("dev-01")).thenReturn(telemetrySchema);

        assertThrows(BaseDomainException.class, () -> service.record(command));
    }

    @Test
    void should_reject_telemetry_when_metric_exceeds_max_range() {
        TelemetryEventIdGenerator idGenerator = mock(TelemetryEventIdGenerator.class);
        TelemetrySchemaResolver telemetrySchemaResolver = mock(TelemetrySchemaResolver.class);
        TelemetryHotDataPort hotDataPort = mock(TelemetryHotDataPort.class);
        TelemetrySnapshotRepository snapshotRepository = mock(TelemetrySnapshotRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TelemetryIngestApplicationService service = new TelemetryIngestApplicationService(
                idGenerator,
                telemetrySchemaResolver,
                (baseMetrics, derivedMetricDefinitions) -> baseMetrics,
                hotDataPort,
                snapshotRepository,
                publisher);
        Instant reportedAt = Instant.parse("2026-05-13T10:00:00Z");
        RecordTelemetryCommand command = new RecordTelemetryCommand(
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(150), null),
                reportedAt,
                "{\"temperature\":150}");
        TelemetrySchema telemetrySchema = new TelemetrySchema(
                "prod-sensor",
                List.of(new TelemetryMetricDefinition(
                        new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("temperature"),
                        true,
                        "C",
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(100))),
                List.of());

        when(telemetrySchemaResolver.resolveByDeviceId("dev-01")).thenReturn(telemetrySchema);

        assertThrows(BaseDomainException.class, () -> service.record(command));
    }

    @Test
    void should_compute_derived_metric_before_validating_and_publishing() {
        TelemetryEventIdGenerator idGenerator = mock(TelemetryEventIdGenerator.class);
        TelemetrySchemaResolver telemetrySchemaResolver = mock(TelemetrySchemaResolver.class);
        TelemetryHotDataPort hotDataPort = mock(TelemetryHotDataPort.class);
        TelemetrySnapshotRepository snapshotRepository = mock(TelemetrySnapshotRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TelemetryIngestApplicationService service = new TelemetryIngestApplicationService(
                idGenerator,
                telemetrySchemaResolver,
                (baseMetrics, derivedMetricDefinitions) -> {
                    java.util.Map<com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName, BigDecimal> values =
                            new java.util.LinkedHashMap<>(baseMetrics.values());
                    values.put(new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("heatIndex"), BigDecimal.valueOf(42));
                    return new TelemetryMetrics(values);
                },
                hotDataPort,
                snapshotRepository,
                publisher);
        Instant reportedAt = Instant.parse("2026-05-13T10:00:00Z");
        RecordTelemetryCommand command = new RecordTelemetryCommand(
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(36.5), BigDecimal.valueOf(55)),
                reportedAt,
                "{\"temperature\":36.5,\"humidity\":55}");
        TelemetrySchema telemetrySchema = new TelemetrySchema(
                "prod-sensor",
                List.of(
                        new TelemetryMetricDefinition(
                                new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("temperature"),
                                true,
                                "C",
                                BigDecimal.ZERO,
                                BigDecimal.valueOf(100)),
                        new TelemetryMetricDefinition(
                                new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("humidity"),
                                true,
                                "%",
                                BigDecimal.ZERO,
                                BigDecimal.valueOf(100))),
                List.of(new DerivedMetricDefinition(
                        new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("heatIndex"),
                        List.of(
                                new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("temperature"),
                                new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("humidity")),
                        "temperature + (humidity * 0.1)",
                        false,
                        "C")));

        when(idGenerator.nextId("dev-01", reportedAt, "{\"temperature\":36.5,\"humidity\":55}")).thenReturn(11L);
        when(snapshotRepository.findByDeviceId(new DeviceId("dev-01"))).thenReturn(Optional.empty());
        when(telemetrySchemaResolver.resolveByDeviceId("dev-01")).thenReturn(telemetrySchema);

        TelemetryEvent result = service.record(command);

        assertEquals("42", result.metrics().valueOf(new com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName("heatIndex")).toPlainString());
    }
}
