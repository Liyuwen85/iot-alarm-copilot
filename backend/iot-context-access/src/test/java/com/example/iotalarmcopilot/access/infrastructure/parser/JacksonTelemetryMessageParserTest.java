package com.example.iotalarmcopilot.access.infrastructure.parser;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.access.application.model.TelemetryMessage;
import com.example.iotalarmcopilot.contract.device.BinaryStateMappingContract;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryModel;
import com.example.iotalarmcopilot.contract.device.TelemetryMetricContract;
import com.example.iotalarmcopilot.contract.device.TelemetryTransformType;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonTelemetryMessageParserTest {

    private final JacksonTelemetryMessageParser parser =
            new JacksonTelemetryMessageParser(new ObjectMapper());

    @Test
    void should_parse_valid_payload() {
        DeviceTelemetryModel deviceTelemetryModel = new DeviceTelemetryModel(
                "dev-01",
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
                List.of());
        String rawJson = """
                {
                  "deviceId":"dev-01",
                  "properties":{"tempC":"81.5"},
                  "reportedAt":"2026-05-13T18:00:00+08:00"
                }
                """;

        TelemetryMessage message = parser.parse(rawJson, deviceTelemetryModel);

        assertEquals("dev-01", message.deviceId());
        assertEquals(BigDecimal.valueOf(81.5), message.metrics().temperature());
        assertEquals(Instant.parse("2026-05-13T10:00:00Z"), message.reportedAt());
    }

    @Test
    void should_reject_invalid_number() {
        DeviceTelemetryModel deviceTelemetryModel = new DeviceTelemetryModel(
                "dev-01",
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
                List.of());
        String rawJson = """
                {
                  "deviceId":"dev-01",
                  "properties":{"tempC":"bad-number"}
                }
                """;

        assertThrows(BaseDomainException.class, () -> parser.parse(rawJson, deviceTelemetryModel));
    }

    @Test
    void should_apply_scale_transform_when_parsing_payload() {
        DeviceTelemetryModel deviceTelemetryModel = new DeviceTelemetryModel(
                "dev-01",
                "prod-sensor",
                List.of(new TelemetryMetricContract(
                        "temperature",
                        "/properties/tempRaw",
                        null,
                        TelemetryTransformType.SCALE,
                        BigDecimal.valueOf(0.1),
                        null,
                        true,
                        "C",
                        null,
                        null)),
                List.of());
        String rawJson = """
                {
                  "deviceId":"dev-01",
                  "properties":{"tempRaw":"815"}
                }
                """;

        TelemetryMessage message = parser.parse(rawJson, deviceTelemetryModel);

        assertEquals(BigDecimal.valueOf(81.5), message.metrics().temperature());
    }

    @Test
    void should_map_binary_state_literal_when_parsing_payload() {
        DeviceTelemetryModel deviceTelemetryModel = new DeviceTelemetryModel(
                "dev-01",
                "prod-sensor",
                List.of(new TelemetryMetricContract(
                        "runningStatus",
                        "/properties/status",
                        new BinaryStateMappingContract(
                                "ON",
                                "OFF",
                                BigDecimal.ONE,
                                BigDecimal.ZERO),
                        TelemetryTransformType.IDENTITY,
                        null,
                        null,
                        false,
                        "state",
                        BigDecimal.ZERO,
                        BigDecimal.ONE)),
                List.of());
        String rawJson = """
                {
                  "deviceId":"dev-01",
                  "properties":{"status":"ON"}
                }
                """;

        TelemetryMessage message = parser.parse(rawJson, deviceTelemetryModel);

        assertEquals(BigDecimal.ONE, message.metrics().valueOf(new TelemetryMetricName("runningStatus")));
    }
}
