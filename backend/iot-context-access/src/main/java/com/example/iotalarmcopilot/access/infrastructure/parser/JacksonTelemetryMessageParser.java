package com.example.iotalarmcopilot.access.infrastructure.parser;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.access.application.model.TelemetryMessage;
import com.example.iotalarmcopilot.access.application.port.TelemetryMessageParser;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryModel;
import com.example.iotalarmcopilot.contract.device.TelemetryMetricContract;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 根据设备产品模型等格式，从原始是数据中解析
 */
@Component
public class JacksonTelemetryMessageParser implements TelemetryMessageParser {

    private final ObjectMapper objectMapper;

    public JacksonTelemetryMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public TelemetryMessage parse(String rawJson, DeviceTelemetryModel deviceTelemetryModel) {
        if (rawJson == null || rawJson.isBlank()) {
            throw new BaseDomainException("Telemetry payload must not be blank");
        }
        if (deviceTelemetryModel == null) {
            throw new BaseDomainException("Device telemetry model must not be null");
        }
        JsonNode root = readPayload(rawJson);
        return new TelemetryMessage(
                readOptionalText(root, "deviceId"),
                readMetrics(root, deviceTelemetryModel),
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

    private TelemetryMetrics readMetrics(JsonNode root, DeviceTelemetryModel deviceTelemetryModel) {
        Map<TelemetryMetricName, BigDecimal> values = new LinkedHashMap<>();
        for (TelemetryMetricContract metricContract : deviceTelemetryModel.metricContracts()) {
            BigDecimal value = readOptionalDecimal(root, metricContract);
            if (value != null) {
                values.put(new TelemetryMetricName(metricContract.metricCode()), value);
            }
        }
        return new TelemetryMetrics(values);
    }

    private BigDecimal readOptionalDecimal(JsonNode root, TelemetryMetricContract metricContract) {
        JsonNode field = root.at(metricContract.sourcePath());
        if (field == null || field.isMissingNode() || field.isNull()) {
            return null;
        }
        try {
            if (metricContract.binaryStateMapping() != null) {
                return metricContract.binaryStateMapping().map(field.asText());
            }
            return applyTransform(new BigDecimal(field.asText()), metricContract);
        } catch (IllegalArgumentException exception) {
            throw new BaseDomainException("Invalid numeric field: " + metricContract.metricCode());
        }
    }

    private BigDecimal applyTransform(BigDecimal rawValue, TelemetryMetricContract metricContract) {
        return switch (metricContract.transformType()) {
            case IDENTITY -> rawValue;
            case SCALE -> rawValue.multiply(metricContract.factor());
            case OFFSET -> rawValue.add(metricContract.offset());
        };
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
