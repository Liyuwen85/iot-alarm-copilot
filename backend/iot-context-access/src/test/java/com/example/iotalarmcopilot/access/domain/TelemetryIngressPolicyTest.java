package com.example.iotalarmcopilot.access.domain;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.BaseDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TelemetryIngressPolicyTest {

    private final TelemetryIngressPolicy policy = new TelemetryIngressPolicy();

    @Test
    void should_normalize_valid_message() {
        TelemetryMessage message = new TelemetryMessage(
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(81.5), null),
                Instant.parse("2026-05-13T10:00:00Z"),
                "{\"deviceId\":\"dev-01\",\"temperature\":\"81.5\"}");

        TelemetryPayload result = policy.normalize("iot/dev-01/telemetry", message);

        assertEquals("dev-01", result.deviceId());
        assertEquals(BigDecimal.valueOf(81.5), result.temperature());
        assertEquals(Instant.parse("2026-05-13T10:00:00Z"), result.reportedAt());
    }

    @Test
    void should_reject_mismatched_payload_device_id() {
        TelemetryMessage message = new TelemetryMessage(
                "dev-02",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(81.5), null),
                Instant.parse("2026-05-13T10:00:00Z"),
                "{\"deviceId\":\"dev-02\",\"temperature\":\"81.5\"}");

        assertThrows(BaseDomainException.class, () ->
                policy.normalize("iot/dev-01/telemetry", message));
    }
}
