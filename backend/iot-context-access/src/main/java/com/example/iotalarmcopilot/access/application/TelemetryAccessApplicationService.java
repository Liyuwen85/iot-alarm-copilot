package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.access.application.model.TelemetryMessage;
import com.example.iotalarmcopilot.access.application.model.TelemetryPayload;
import com.example.iotalarmcopilot.access.application.model.TelemetryTopic;
import com.example.iotalarmcopilot.access.application.port.TelemetryMessageParser;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryIngestionPort;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryModel;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryModelQueryPort;
import com.example.iotalarmcopilot.telemetry.application.RecordTelemetryCommand;
import com.example.iotalarmcopilot.telemetry.application.TelemetryIngestApplicationService;
import org.springframework.stereotype.Service;

/**
 * 接收设备上报的遥测数据
 */
@Service
public class TelemetryAccessApplicationService {

    private final TelemetryMessageParser telemetryMessageParser;
    private final DeviceTelemetryIngestionPort deviceTelemetryIngestionPort;
    private final DeviceTelemetryModelQueryPort deviceTelemetryModelQueryPort;
    private final TelemetryIngestApplicationService telemetryIngestApplicationService;
    private final TelemetryIngressPolicy telemetryIngressPolicy = new TelemetryIngressPolicy();

    public TelemetryAccessApplicationService(
            TelemetryMessageParser telemetryMessageParser,
            DeviceTelemetryIngestionPort deviceTelemetryIngestionPort,
            DeviceTelemetryModelQueryPort deviceTelemetryModelQueryPort,
            TelemetryIngestApplicationService telemetryIngestApplicationService) {
        this.telemetryMessageParser = telemetryMessageParser;
        this.deviceTelemetryIngestionPort = deviceTelemetryIngestionPort;
        this.deviceTelemetryModelQueryPort = deviceTelemetryModelQueryPort;
        this.telemetryIngestApplicationService = telemetryIngestApplicationService;
    }

    public void ingestMqttTelemetry(String topic, String payload) {
        ingestTelemetry(topic, payload);
    }

    public void ingestKafkaTelemetry(String topic, String payload) {
        ingestTelemetry(topic, payload);
    }

    /**
     * 接收原始遥测数据
     *
     * @param topic   MQTT主题
     * @param payload MQTT消息内容
     */
    public void ingestTelemetry(String topic, String payload) {
        TelemetryTopic telemetryTopic = new TelemetryTopic(topic);
        String topicDeviceId = telemetryTopic.deviceId();
        // 确保设备允许上报遥测数据
        deviceTelemetryIngestionPort.ensureTelemetryIngestionAllowed(topicDeviceId);
        // 设备遥测模型
        DeviceTelemetryModel deviceTelemetryModel = deviceTelemetryModelQueryPort.findTelemetryModel(topicDeviceId)
                .orElseThrow(() -> new BaseDomainException("Device telemetry model not found. deviceCode=" + topicDeviceId));
        // 根据模型解析数据
        TelemetryMessage telemetryMessage = telemetryMessageParser.parse(payload, deviceTelemetryModel);
        TelemetryPayload telemetryPayload = telemetryIngressPolicy.normalize(telemetryTopic, telemetryMessage);
        // 存储遥测数据
        telemetryIngestApplicationService.record(new RecordTelemetryCommand(
                telemetryPayload.deviceId(),
                telemetryPayload.metrics(),
                telemetryPayload.reportedAt(),
                telemetryPayload.rawJson()));
    }
}
