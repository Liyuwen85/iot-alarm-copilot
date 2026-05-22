package com.example.iotalarmcopilot.access.interfaces.kafka;

import com.example.iotalarmcopilot.access.application.TelemetryAccessApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 上行遥测正式主入口。
 */
@Slf4j
@Component
public class KafkaTelemetryConsumer {

    private final KafkaAccessProperties properties;
    private final TelemetryAccessApplicationService telemetryAccessApplicationService;

    public KafkaTelemetryConsumer(
            KafkaAccessProperties properties,
            TelemetryAccessApplicationService telemetryAccessApplicationService) {
        this.properties = properties;
        this.telemetryAccessApplicationService = telemetryAccessApplicationService;
    }

    /**
     * Kafka 上行遥测消息处理入口
     * @param envelope
     */
    @KafkaListener(
            topics = "${iot.kafka.telemetry-topic:iot.telemetry.raw}",
            groupId = "${iot.kafka.consumer-group:iot-platform-access}",
            autoStartup = "${iot.kafka.enabled:true}")
    public void onMessage(KafkaTelemetryEnvelope envelope) {
        try {
            telemetryAccessApplicationService.ingestKafkaTelemetry(envelope.topic(), envelope.payload());
            log.info("Telemetry ingested from kafka topic={}, mqttTopic={}",
                    properties.getTelemetryTopic(),
                    envelope.topic());
        } catch (Exception exception) {
            log.error("Failed to process telemetry message from kafka. kafkaTopic={}, mqttTopic={}, payload={}",
                    properties.getTelemetryTopic(),
                    envelope.topic(),
                    envelope.payload(),
                    exception);
            throw exception;
        }
    }
}
