package com.example.iotalarmcopilot.mockdevice;

import com.example.iotalarmcopilot.mockdevice.application.GatewayServerService;
import com.example.iotalarmcopilot.mockdevice.application.GatewayTelemetryDeduplicator;
import com.example.iotalarmcopilot.mockdevice.application.GatewayTelemetryForwarder;
import com.example.iotalarmcopilot.mockdevice.application.GatewayTelemetryPublishScheduler;
import com.example.iotalarmcopilot.mockdevice.application.MockDeviceTelemetryService;
import com.example.iotalarmcopilot.mockdevice.application.port.Lwm2mServerRuntime;
import com.example.iotalarmcopilot.mockdevice.config.Lwm2mGatewayConfig;
import com.example.iotalarmcopilot.mockdevice.config.MockDeviceConfig;
import com.example.iotalarmcopilot.mockdevice.infrastructure.lwm2m.LeshanLwm2mServer;
import com.example.iotalarmcopilot.mockdevice.infrastructure.mqtt.PahoMqttMessagePublisher;
import com.example.iotalarmcopilot.mockdevice.infrastructure.mqtt.PahoMqttSubscriberClientProvider;
import com.example.iotalarmcopilot.mockdevice.interfaces.mqtt.GatewayMqttCommandConsumer;
import com.example.iotalarmcopilot.mockdevice.interfaces.mqtt.MockDeviceMqttCommandConsumer;
import com.example.iotalarmcopilot.mockdevice.support.MockDeviceLoggers;
import org.slf4j.Logger;

public final class MockDeviceApplication {

    private static final Logger DEVICE_LOGGER = MockDeviceLoggers.deviceLogger();
    private static final Logger GATEWAY_LOGGER = MockDeviceLoggers.gatewayLogger();

    private MockDeviceApplication() {
    }

    public static void main(String[] args) {
        MockDeviceConfig mockDeviceConfig = MockDeviceConfig.load();
        Lwm2mGatewayConfig lwm2mGatewayConfig = Lwm2mGatewayConfig.load();
        DEVICE_LOGGER.info(
                "mock-device starting deviceId={} telemetryTopic={} intervalMs={} maxMessages={}",
                mockDeviceConfig.deviceId(),
                mockDeviceConfig.telemetryTopic(),
                mockDeviceConfig.intervalMs(),
                mockDeviceConfig.maxMessages());

        PahoMqttMessagePublisher mockDeviceMqttMessagePublisher =
                new PahoMqttMessagePublisher(mockDeviceConfig.brokerUrl(), mockDeviceConfig.clientId(), DEVICE_LOGGER);
        MockDeviceTelemetryService mockDeviceTelemetryService = new MockDeviceTelemetryService(
                mockDeviceConfig,
                mockDeviceMqttMessagePublisher);

        MockDeviceMqttCommandConsumer mockDeviceMqttCommandConsumer = new MockDeviceMqttCommandConsumer(
                new PahoMqttSubscriberClientProvider(
                        mockDeviceConfig.brokerUrl(),
                        mockDeviceConfig.clientId() + "-cmd",
                        mockDeviceConfig.commandTopic(),
                        mockDeviceConfig.qos()),
                mockDeviceTelemetryService);
        mockDeviceMqttCommandConsumer.subscribe();

        PahoMqttMessagePublisher gatewayMqttMessagePublisher =
                new PahoMqttMessagePublisher(lwm2mGatewayConfig.brokerUrl(), lwm2mGatewayConfig.mqttClientId(), GATEWAY_LOGGER);

        GatewayTelemetryPublishScheduler gatewayTelemetryPublishScheduler =
                new GatewayTelemetryPublishScheduler(150L);
        GatewayTelemetryDeduplicator gatewayTelemetryDeduplicator =
                new GatewayTelemetryDeduplicator(5000L);
        GatewayTelemetryForwarder gatewayTelemetryForwarder = new GatewayTelemetryForwarder(
                lwm2mGatewayConfig,
                gatewayMqttMessagePublisher,
                gatewayTelemetryPublishScheduler,
                gatewayTelemetryDeduplicator);

        Lwm2mServerRuntime lwm2MServerRuntime = new LeshanLwm2mServer(lwm2mGatewayConfig, gatewayTelemetryForwarder);

        GatewayServerService gatewayServerService = new GatewayServerService(
                lwm2mGatewayConfig,
                lwm2MServerRuntime,
                gatewayMqttMessagePublisher);

        GatewayMqttCommandConsumer gatewayMqttCommandConsumer = new GatewayMqttCommandConsumer(
                new PahoMqttSubscriberClientProvider(
                        lwm2mGatewayConfig.brokerUrl(),
                        lwm2mGatewayConfig.mqttClientId() + "-cmd",
                        lwm2mGatewayConfig.commandTopicFilter(),
                        lwm2mGatewayConfig.mqttQos()),
                gatewayServerService);
        gatewayMqttCommandConsumer.subscribe();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            mockDeviceMqttCommandConsumer.close();
            mockDeviceTelemetryService.stop();
            mockDeviceMqttMessagePublisher.close();

            gatewayMqttCommandConsumer.close();
            gatewayServerService.stop();

            gatewayTelemetryForwarder.close();
            gatewayMqttMessagePublisher.close();
        }));

        try {
            gatewayServerService.start();
            GATEWAY_LOGGER.info("mock-device gateway started");
        } catch (Exception exception) {
            GATEWAY_LOGGER.warn("mock-device gateway start failed reason={}", exception.getMessage());
        }
        mockDeviceTelemetryService.start();
        DEVICE_LOGGER.info("mock-device telemetry service started");
        mockDeviceTelemetryService.awaitCompletion();
        DEVICE_LOGGER.info("mock-device telemetry service completed");
    }
}
