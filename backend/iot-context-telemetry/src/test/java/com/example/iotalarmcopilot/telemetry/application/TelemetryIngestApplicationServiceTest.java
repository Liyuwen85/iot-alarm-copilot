package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.contract.event.TelemetryRecordedEvent;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.telemetry.domain.DeviceId;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEventIdGenerator;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshot;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelemetryIngestApplicationServiceTest {

    @Test
    void should_record_telemetry_and_publish_event() {
        TelemetryEventIdGenerator idGenerator = mock(TelemetryEventIdGenerator.class);
        TelemetryHotDataPort hotDataPort = mock(TelemetryHotDataPort.class);
        TelemetrySnapshotRepository snapshotRepository = mock(TelemetrySnapshotRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TelemetryIngestApplicationService service = new TelemetryIngestApplicationService(
                idGenerator,
                hotDataPort,
                snapshotRepository,
                publisher);
        Instant reportedAt = Instant.parse("2026-05-13T10:00:00Z");
        RecordTelemetryCommand command = new RecordTelemetryCommand(
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(36.5), null),
                reportedAt,
                "{\"temperature\":36.5}");

        when(idGenerator.nextId("dev-01", reportedAt, "{\"temperature\":36.5}")).thenReturn(11L);
        when(snapshotRepository.findByDeviceId(new DeviceId("dev-01"))).thenReturn(Optional.empty());

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
}
