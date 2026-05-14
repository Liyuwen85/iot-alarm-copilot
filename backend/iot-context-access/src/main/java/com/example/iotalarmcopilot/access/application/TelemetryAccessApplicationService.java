package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.access.domain.TelemetryIngressPolicy;
import com.example.iotalarmcopilot.access.domain.TelemetryMessage;
import com.example.iotalarmcopilot.access.domain.TelemetryMessageParser;
import com.example.iotalarmcopilot.access.domain.TelemetryPayload;
import com.example.iotalarmcopilot.telemetry.application.RecordTelemetryCommand;
import com.example.iotalarmcopilot.telemetry.application.TelemetryIngestApplicationService;
import org.springframework.stereotype.Service;

/**
 * 遥测接入应用服务
 */
@Service
public class TelemetryAccessApplicationService {

    private final TelemetryMessageParser telemetryMessageParser;
    private final TelemetryIngestApplicationService telemetryIngestApplicationService;
    private final TelemetryIngressPolicy telemetryIngressPolicy = new TelemetryIngressPolicy();

    public TelemetryAccessApplicationService(
            TelemetryMessageParser telemetryMessageParser,
            TelemetryIngestApplicationService telemetryIngestApplicationService) {
        this.telemetryMessageParser = telemetryMessageParser;
        this.telemetryIngestApplicationService = telemetryIngestApplicationService;
    }

    /**
     * 接收MQTT遥测数据
     *
     * @param topic
     * @param payload
     */
    public void ingestMqttTelemetry(String topic, String payload) {
        ingestTelemetry(topic, payload);
    }

    /**
     * 接收Kafka遥测数据
     *
     * @param topic
     * @param payload
     */
    public void ingestKafkaTelemetry(String topic, String payload) {
        ingestTelemetry(topic, payload);
    }

    public void ingestTelemetry(String topic, String payload) {
        // 解析原始遥测数据
        TelemetryMessage telemetryMessage = telemetryMessageParser.parse(payload);
        // 规范化遥测数据
        TelemetryPayload telemetryPayload = telemetryIngressPolicy.normalize(topic, telemetryMessage);
        telemetryIngestApplicationService.record(new RecordTelemetryCommand(
                telemetryPayload.deviceId(),
                telemetryPayload.metrics(),
                telemetryPayload.reportedAt(),
                telemetryPayload.rawJson()));
    }
}
