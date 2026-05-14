package com.example.iotalarmcopilot.access.infrastructure.parser;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.access.domain.TelemetryMessageParser;
import com.example.iotalarmcopilot.access.domain.TelemetryMessage;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * 解析 MQTT 遥测数据
 */
@Component
public class JacksonTelemetryMessageParser implements TelemetryMessageParser {

    private final ObjectMapper objectMapper;

    public JacksonTelemetryMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public TelemetryMessage parse(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            throw new BaseDomainException("Telemetry payload must not be blank");
        }
        JsonNode root = readPayload(rawJson);
        return new TelemetryMessage(
                readOptionalText(root, "deviceId"),
                TelemetryMetrics.ofTemperatureAndHumidity(
                        readOptionalDecimal(root, "temperature"),
                        readOptionalDecimal(root, "humidity")),
                readReportedAt(root),
                rawJson);
    }

    private JsonNode readPayload(String rawJson) {
        try {
            return objectMapper.readTree(rawJson);
        } catch (JsonProcessingException exception) {
            throw new BaseDomainException("Invalid telemetry payload: " + exception.getOriginalMessage());
        }
    }

    private BigDecimal readOptionalDecimal(JsonNode root, String fieldName) {
        JsonNode field = root.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        try {
            return new BigDecimal(field.asText());
        } catch (NumberFormatException exception) {
            throw new BaseDomainException("Invalid numeric field: " + fieldName);
        }
    }

    private Instant readReportedAt(JsonNode root) {
        String value = readOptionalText(root, "ts");
        if (value == null) {
            value = readOptionalText(root, "reportedAt");
        }
        if (value == null || value.isBlank()) {
            return Instant.now();
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (Exception exception) {
            throw new BaseDomainException("Invalid timestamp field: " + value);
        }
    }

    private String readOptionalText(JsonNode root, String fieldName) {
        JsonNode field = root.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        String value = field.asText();
        return value == null ? null : value.trim();
    }
}
