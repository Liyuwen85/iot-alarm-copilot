package com.example.iotalarmcopilot.mockdevice.interfaces.mqtt;

import com.example.iotalarmcopilot.mockdevice.application.MockDeviceTelemetryService;
import com.example.iotalarmcopilot.mockdevice.domain.SetReportIntervalCommandPayload;
import com.example.iotalarmcopilot.mockdevice.support.MockDeviceLoggers;
import org.slf4j.Logger;

public class MockDeviceMqttCommandConsumer extends AbstractMqttCommandConsumer {

    private static final Logger DEVICE_LOGGER = MockDeviceLoggers.deviceLogger();

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

    @Override
    protected Logger logger() {
        return DEVICE_LOGGER;
    }
}
