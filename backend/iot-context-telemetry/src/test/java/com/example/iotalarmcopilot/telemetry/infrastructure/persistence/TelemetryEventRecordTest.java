package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelemetryEventRecordTest {

    @Test
    void should_preserve_dynamic_metrics_when_mapping_record_round_trip() {
        Map<TelemetryMetricName, BigDecimal> values = new LinkedHashMap<>();
        values.put(new TelemetryMetricName("temperature"), BigDecimal.valueOf(36.5));
        values.put(new TelemetryMetricName("humidity"), BigDecimal.valueOf(55));
        values.put(new TelemetryMetricName("heatIndex"), BigDecimal.valueOf(42.0));
        values.put(new TelemetryMetricName("runningStatus"), BigDecimal.ONE);
        TelemetryEvent event = TelemetryEvent.record(
                11L,
                "dev-01",
                new TelemetryMetrics(values),
                Instant.parse("2026-05-18T10:00:00Z"),
                "{\"temperature\":36.5,\"humidity\":55,\"status\":\"ON\"}");

        TelemetryEvent restored = TelemetryEventRecord.fromDomain(event).toDomain();

        assertEquals("36.5", restored.metrics().valueOf(new TelemetryMetricName("temperature")).toPlainString());
        assertEquals("55", restored.metrics().valueOf(new TelemetryMetricName("humidity")).toPlainString());
        assertEquals("42.0", restored.metrics().valueOf(new TelemetryMetricName("heatIndex")).toPlainString());
        assertEquals("1", restored.metrics().valueOf(new TelemetryMetricName("runningStatus")).toPlainString());
    }
}
