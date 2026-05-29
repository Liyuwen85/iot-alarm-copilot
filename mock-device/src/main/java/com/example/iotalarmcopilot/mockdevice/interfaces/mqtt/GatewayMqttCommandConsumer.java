package com.example.iotalarmcopilot.mockdevice.interfaces.mqtt;

import com.example.iotalarmcopilot.mockdevice.application.GatewayServerService;
import com.example.iotalarmcopilot.mockdevice.domain.SetReportIntervalCommandPayload;

/**
 * Gateway的MQTT命令消费者
 */
public class GatewayMqttCommandConsumer extends AbstractMqttCommandConsumer {

    private final GatewayServerService gatewayServerService;

    public GatewayMqttCommandConsumer(MqttSubscriberClientProvider clientProvider,
                                      GatewayServerService gatewayServerService) {
        super(clientProvider);
        this.gatewayServerService = gatewayServerService;
    }

    @Override
    protected void process(SetReportIntervalCommandPayload payload) {
        gatewayServerService.processSetReportIntervalCommandPayload(payload);
    }
}
