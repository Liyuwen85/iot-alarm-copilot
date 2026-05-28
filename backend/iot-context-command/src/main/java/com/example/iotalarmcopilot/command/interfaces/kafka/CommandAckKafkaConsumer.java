package com.example.iotalarmcopilot.command.interfaces.kafka;

import com.example.iotalarmcopilot.command.application.CommandAckApplicationService;
import com.example.iotalarmcopilot.command.application.CommandAckPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * kafka"命令确认”消费者
 */
@Component
public class CommandAckKafkaConsumer {

    private final CommandAckApplicationService commandAckApplicationService;
    private final ObjectMapper objectMapper;

    public CommandAckKafkaConsumer(
            CommandAckApplicationService commandAckApplicationService,
            ObjectMapper objectMapper) {
        this.commandAckApplicationService = commandAckApplicationService;
        this.objectMapper = objectMapper;
    }

    /**
     * 监听命令消费确认消息
     *
     * @param envelopeJson
     */
    @KafkaListener(
            topics = "${iot.kafka.command-ack-topic:iot.command.ack.raw}",
            groupId = "${iot.kafka.consumer-group:iot-platform-access}",
            autoStartup = "${iot.kafka.enabled:true}")
    public void onMessage(String envelopeJson) {
        try {
            KafkaCommandEnvelope envelope = objectMapper.readValue(envelopeJson, KafkaCommandEnvelope.class);
            JsonNode payload = objectMapper.readTree(envelope.payload());
            commandAckApplicationService.handleAck(new CommandAckPayload(
                    payload.path("commandId").asText(),
                    payload.path("deviceId").asText(),
                    payload.path("status").asText(),
                    Instant.parse(payload.path("ackedAt").asText()),
                    payload.path("message").asText("")));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to process command ack message", exception);
        }
    }
}
