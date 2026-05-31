package com.example.iotalarmcopilot.mockdevice.interfaces.mqtt;

import com.example.iotalarmcopilot.mockdevice.application.GatewayServerService;
import com.example.iotalarmcopilot.mockdevice.domain.SetReportIntervalCommandPayload;
import com.example.iotalarmcopilot.mockdevice.support.MockDeviceLoggers;
import org.slf4j.Logger;

public class GatewayMqttCommandConsumer extends AbstractMqttCommandConsumer {

    private static final Logger GATEWAY_LOGGER = MockDeviceLoggers.gatewayLogger();

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

    @Override
    protected Logger logger() {
        return GATEWAY_LOGGER;
    }
}
