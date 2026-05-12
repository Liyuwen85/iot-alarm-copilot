package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.shared.BaseDomainException;
import com.example.iotalarmcopilot.telemetry.application.RecordTelemetryCommand;
import com.example.iotalarmcopilot.telemetry.application.TelemetryIngestApplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * 遥测数据访问应用服务
 */
@Service
public class TelemetryAccessApplicationService {

    private static final String TOPIC_PREFIX = "iot";
    private static final String TOPIC_SUFFIX = "telemetry";

    private final ObjectMapper objectMapper;
    // 遥测领域服务
    private final TelemetryIngestApplicationService telemetryIngestApplicationService;

    public TelemetryAccessApplicationService(
            ObjectMapper objectMapper,
            TelemetryIngestApplicationService telemetryIngestApplicationService) {
        this.objectMapper = objectMapper;
        this.telemetryIngestApplicationService = telemetryIngestApplicationService;
    }

    /**
     * 接收MQTT遥测数据
     *
     * @param topic
     * @param payload
     */
    public void ingestMqttTelemetry(String topic, String payload) {
        TelemetryIngressCommand command = normalize(topic, payload);
        telemetryIngestApplicationService.record(new RecordTelemetryCommand(
                command.deviceId(),
                command.temperature(),
                command.humidity(),
                command.reportedAt(),
                command.rawJson()));
    }

    /**
     * 标准化处理
     *
     * @param topic
     * @param payload
     * @return
     */
    private TelemetryIngressCommand normalize(String topic, String payload) {
        if (topic == null || topic.isBlank()) {
            throw new BaseDomainException("Telemetry topic must not be blank");
        }
        if (payload == null || payload.isBlank()) {
            throw new BaseDomainException("Telemetry payload must not be blank");
        }
        String deviceId = extractDeviceId(topic);
        JsonNode root = readPayload(payload);
        validatePayloadDeviceId(deviceId, readOptionalText(root, "deviceId"));
        return new TelemetryIngressCommand(
                deviceId,
                readOptionalDecimal(root, "temperature"),
                readOptionalDecimal(root, "humidity"),
                readReportedAt(root),
                payload);
    }

    private String extractDeviceId(String topic) {
        String[] segments = topic.split("/");
        if (segments.length != 3 || !TOPIC_PREFIX.equals(segments[0]) || !TOPIC_SUFFIX.equals(segments[2])) {
            throw new BaseDomainException("Unsupported telemetry topic: " + topic);
        }
        if (segments[1].isBlank()) {
            throw new BaseDomainException("Device id in topic must not be blank");
        }
        return segments[1];
    }

    private JsonNode readPayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException exception) {
            throw new BaseDomainException("Invalid telemetry payload: " + exception.getOriginalMessage());
        }
    }

    private void validatePayloadDeviceId(String topicDeviceId, String payloadDeviceId) {
        if (payloadDeviceId != null && !payloadDeviceId.isBlank() && !topicDeviceId.equals(payloadDeviceId)) {
            throw new BaseDomainException("Payload deviceId does not match topic deviceId");
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
