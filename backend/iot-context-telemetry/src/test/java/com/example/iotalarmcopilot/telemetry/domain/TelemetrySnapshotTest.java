package com.example.iotalarmcopilot.telemetry.domain;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelemetrySnapshotTest {

    @Test
    void should_merge_newer_partial_metrics_into_snapshot() {
        TelemetryEvent first = TelemetryEvent.record(
                10L,
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(21.5), null),
                Instant.parse("2026-05-14T10:00:00Z"),
                "{\"temperature\":21.5}");
        TelemetryEvent second = TelemetryEvent.record(
                11L,
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(null, BigDecimal.valueOf(60.2)),
                Instant.parse("2026-05-14T10:01:00Z"),
                "{\"humidity\":60.2}");

        TelemetrySnapshot snapshot = TelemetrySnapshot.capture(first).refreshBy(second);

        assertEquals("21.5", snapshot.temperature().toPlainString());
        assertEquals("60.2", snapshot.humidity().toPlainString());
        assertEquals(11L, snapshot.lastTelemetryEventId());
    }

    @Test
    void should_ignore_older_event_when_refreshing_snapshot() {
        TelemetryEvent latest = TelemetryEvent.record(
                20L,
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(21.5), BigDecimal.valueOf(60.2)),
                Instant.parse("2026-05-14T10:01:00Z"),
                "{\"temperature\":21.5,\"humidity\":60.2}");
        TelemetryEvent older = TelemetryEvent.record(
                19L,
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(19.8), null),
                Instant.parse("2026-05-14T10:00:00Z"),
                "{\"temperature\":19.8}");

        TelemetrySnapshot snapshot = TelemetrySnapshot.capture(latest).refreshBy(older);

        assertEquals("21.5", snapshot.temperature().toPlainString());
        assertEquals("60.2", snapshot.humidity().toPlainString());
        assertEquals(20L, snapshot.lastTelemetryEventId());
    }
}
