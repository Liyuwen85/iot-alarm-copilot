package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.contract.event.TelemetryRecordedEvent;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelemetryIngestApplicationServiceTest {

    @Test
    void should_record_telemetry_and_publish_event() {
        TelemetryEventRepository repository = mock(TelemetryEventRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TelemetryIngestApplicationService service = new TelemetryIngestApplicationService(repository, publisher);
        Instant reportedAt = Instant.parse("2026-05-13T10:00:00Z");
        RecordTelemetryCommand command = new RecordTelemetryCommand(
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(36.5), null),
                reportedAt,
                "{\"temperature\":36.5}");
        TelemetryEvent savedEvent = new TelemetryEvent(
                11L,
                new com.example.iotalarmcopilot.telemetry.domain.DeviceId("dev-01"),
                TelemetryMetrics.ofTemperatureAndHumidity(
                        BigDecimal.valueOf(36.5),
                        null),
                reportedAt,
                "{\"temperature\":36.5}");

        when(repository.save(any(TelemetryEvent.class))).thenReturn(savedEvent);

        TelemetryEvent result = service.record(command);

        assertEquals(11L, result.id());
        verify(repository).save(any(TelemetryEvent.class));
        verify(publisher).publishEvent(new TelemetryRecordedEvent(
                11L,
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(36.5), null),
                reportedAt));
    }
}
