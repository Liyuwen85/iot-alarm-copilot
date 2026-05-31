package com.example.iotalarmcopilot.mockdevice.application;

import com.example.iotalarmcopilot.mockdevice.application.port.Lwm2mServerRuntime;
import com.example.iotalarmcopilot.mockdevice.application.port.MqttMessagePublisher;
import com.example.iotalarmcopilot.mockdevice.config.Lwm2mGatewayConfig;
import com.example.iotalarmcopilot.mockdevice.domain.CommandAckPayload;
import com.example.iotalarmcopilot.mockdevice.domain.InvalidReportIntervalException;
import com.example.iotalarmcopilot.mockdevice.domain.SetReportIntervalCommandPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;

/**
 * lwm2m网关服务
 */
public class GatewayServerService {

    private final Lwm2mGatewayConfig config;
    private final Lwm2mServerRuntime lwm2MServerRuntime;
    private final MqttMessagePublisher mqttMessagePublisher;

    private final ObjectMapper objectMapper;

    public GatewayServerService(Lwm2mGatewayConfig config,
                                Lwm2mServerRuntime lwM2MServerRuntime,
                                MqttMessagePublisher mqttMessagePublisher) {
        this.config = config;
        this.lwm2MServerRuntime = lwM2MServerRuntime;
        this.objectMapper = new ObjectMapper();
        this.mqttMessagePublisher = mqttMessagePublisher;
    }

    public void start() {
        lwm2MServerRuntime.start();
    }

    public void stop() {
        lwm2MServerRuntime.stop();
    }

    /**
     * 下行命令处理
     * @param command
     */
    public void processSetReportIntervalCommandPayload(SetReportIntervalCommandPayload command) {
        if (!"set_report_interval".equalsIgnoreCase(command.commandType())) {
            publishAck(new CommandAckPayload(
                    command.commandId(),
                    command.deviceId(),
                    "FAILED",
                    OffsetDateTime.now().toString(),
                    "unsupported command type"));
            return;
        }
        if (command.params() == null || command.params().intervalMs() < 500) {
            publishAck(new CommandAckPayload(
                    command.commandId(),
                    command.deviceId(),
                    "FAILED",
                    OffsetDateTime.now().toString(),
                    "invalid interval"));
            return;
        }

        try {
            boolean delivered = lwm2MServerRuntime.setReportInterval(command);
            if (!delivered) {
                publishAck(new CommandAckPayload(
                        command.commandId(),
                        command.deviceId(),
                        "FAILED",
                        OffsetDateTime.now().toString(),
                        "device is offline or not registered"));
                return;
            }
            String ackTopic = config.commandAckTopicPattern().replace("{deviceId}", command.deviceId());

            // 成功回复
            publishAck(new CommandAckPayload(
                    command.commandId(),
                    command.deviceId(),
                    "SUCCESS",
                    OffsetDateTime.now().toString(),
                    "interval changed to " + command.params().intervalMs()));

            System.out.printf("command-applied source=gateway deviceId=%s ackTopic=%s commandId=%s intervalMs=%d%n",
                    command.deviceId(),
                    ackTopic,
                    command.commandId(),
                    command.params().intervalMs());
        } catch (InvalidReportIntervalException e) {
            publishAck(new CommandAckPayload(
                    command.commandId(),
                    command.deviceId(),
                    "FAILED",
                    OffsetDateTime.now().toString(),
                    e.getMessage()));
        }
    }

    private void publishAck(CommandAckPayload ackPayload) {
        String ackJson = null;
        try {
            ackJson = objectMapper.writeValueAsString(ackPayload);
            String ackTopic = config.commandAckTopicPattern().replace("{deviceId}", ackPayload.deviceId());
            mqttMessagePublisher.publish(ackTopic, ackJson, config.mqttQos());
            System.out.printf("command-ack-published source=gateway topic=%s payload=%s%n", ackTopic, ackJson);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize ack payload", e);
        }
    }

}
