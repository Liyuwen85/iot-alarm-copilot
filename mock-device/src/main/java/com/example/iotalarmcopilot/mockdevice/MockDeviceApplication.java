package com.example.iotalarmcopilot.mockdevice;

import com.example.iotalarmcopilot.mockdevice.application.*;
import com.example.iotalarmcopilot.mockdevice.application.port.Lwm2mServerRuntime;
import com.example.iotalarmcopilot.mockdevice.config.Lwm2mGatewayConfig;
import com.example.iotalarmcopilot.mockdevice.config.MockDeviceConfig;
import com.example.iotalarmcopilot.mockdevice.infrastructure.lwm2m.LeshanLwm2mServer;
import com.example.iotalarmcopilot.mockdevice.infrastructure.mqtt.PahoMqttMessagePublisher;
import com.example.iotalarmcopilot.mockdevice.infrastructure.mqtt.PahoMqttSubscriberClientProvider;
import com.example.iotalarmcopilot.mockdevice.interfaces.mqtt.GatewayMqttCommandConsumer;
import com.example.iotalarmcopilot.mockdevice.interfaces.mqtt.MockDeviceMqttCommandConsumer;

/**
 * 实现：1. 模拟设备001；2. 模拟LwM2M型网关。
 */
public final class MockDeviceApplication {

    private MockDeviceApplication() {
    }

    public static void main(String[] args) {
        MockDeviceConfig mockDeviceConfig = MockDeviceConfig.load();
        Lwm2mGatewayConfig lwm2mGatewayConfig = Lwm2mGatewayConfig.load();

        // mock-device上报服务
        PahoMqttMessagePublisher mockDeviceMqttMessagePublisher = new PahoMqttMessagePublisher(mockDeviceConfig.brokerUrl(), mockDeviceConfig.clientId());
        MockDeviceTelemetryService mockDeviceTelemetryService = new MockDeviceTelemetryService(
                mockDeviceConfig,
                mockDeviceMqttMessagePublisher);

        // mock-device命令消费者
        MockDeviceMqttCommandConsumer mockDeviceMqttCommandConsumer = new MockDeviceMqttCommandConsumer(
                new PahoMqttSubscriberClientProvider(
                        mockDeviceConfig.brokerUrl(),
                        mockDeviceConfig.clientId() + "-cmd",
                        mockDeviceConfig.commandTopic(),
                        mockDeviceConfig.qos()),
                mockDeviceTelemetryService);
        mockDeviceMqttCommandConsumer.subscribe();

        // gateway上报服务
        PahoMqttMessagePublisher gatewayMqttMessagePublisher = new PahoMqttMessagePublisher(lwm2mGatewayConfig.brokerUrl(), lwm2mGatewayConfig.mqttClientId());

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

        // gateway命令消费者
        GatewayMqttCommandConsumer gatewayMqttCommandConsumer = new GatewayMqttCommandConsumer(
                new PahoMqttSubscriberClientProvider(lwm2mGatewayConfig.brokerUrl(),
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

        gatewayServerService.start();
        mockDeviceTelemetryService.start();
        mockDeviceTelemetryService.awaitCompletion();
    }
}
