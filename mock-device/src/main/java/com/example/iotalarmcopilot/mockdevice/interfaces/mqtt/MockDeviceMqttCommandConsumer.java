package com.example.iotalarmcopilot.mockdevice.interfaces.mqtt;

import com.example.iotalarmcopilot.mockdevice.application.MockDeviceTelemetryService;
import com.example.iotalarmcopilot.mockdevice.domain.SetReportIntervalCommandPayload;

/**
 * 模拟设备MQTT命令消费者
 */
public class MockDeviceMqttCommandConsumer extends AbstractMqttCommandConsumer {

    private final MockDeviceTelemetryService telemetryService;

    public MockDeviceMqttCommandConsumer(MqttSubscriberClientProvider clientProvider,
                                         MockDeviceTelemetryService telemetryService) {
        super(clientProvider);
        this.telemetryService = telemetryService;
    }

    @Override
    protected void process(SetReportIntervalCommandPayload payload) {
        telemetryService.processSetReportIntervalCommandPayload(payload);
    }
}
