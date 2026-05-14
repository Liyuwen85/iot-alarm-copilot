package com.example.iotalarmcopilot.telemetry.domain;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.BaseDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TelemetryEventTest {

    @Test
    void should_build_metrics_backed_event() {
        TelemetryEvent event = TelemetryEvent.record(
                "dev-01",
                BigDecimal.valueOf(36.5),
                BigDecimal.valueOf(50),
                Instant.parse("2026-05-13T10:00:00Z"),
                "{\"temperature\":36.5,\"humidity\":50}");

        assertEquals(BigDecimal.valueOf(36.5), event.temperature());
        assertEquals(BigDecimal.valueOf(50), event.humidity());
        assertEquals(BigDecimal.valueOf(36.5), event.metrics().temperature());
    }

    @Test
    void should_reject_empty_metrics() {
        assertThrows(BaseDomainException.class, () -> new TelemetryEvent(
                1L,
                new DeviceId("dev-01"),
                new TelemetryMetrics(java.util.Map.of()),
                Instant.parse("2026-05-13T10:00:00Z"),
                "{}"));
    }
}
