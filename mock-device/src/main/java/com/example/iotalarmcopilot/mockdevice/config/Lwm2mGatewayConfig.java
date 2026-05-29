package com.example.iotalarmcopilot.mockdevice.config;

/**
 * LwM2M网关配置
 *
 * @param enabled
 * @param gatewayId
 * @param bindHost
 * @param bindPort
 * @param brokerUrl
 * @param mqttClientId
 * @param mqttQos
 * @param topicPattern
 * @param commandTopicFilter
 * @param commandAckTopicPattern
 * @param commandConsumerClientId
 */
public record Lwm2mGatewayConfig(
        boolean enabled,
        String gatewayId,
        String bindHost,
        int bindPort,
        String brokerUrl,
        String mqttClientId,
        int mqttQos,
        String topicPattern,
        String commandTopicFilter,
        String commandAckTopicPattern,
        String commandConsumerClientId) {

    public static Lwm2mGatewayConfig load() {
        String gatewayId = MockDeviceConfig.read("mock.lwm2m.gatewayId", "MOCK_LWM2M_GATEWAY_ID", "mock-gateway-01");
        return new Lwm2mGatewayConfig(
                Boolean.parseBoolean(MockDeviceConfig.read("mock.lwm2m.enabled", "MOCK_LWM2M_ENABLED", "true")),
                gatewayId,
                MockDeviceConfig.read("mock.lwm2m.bindHost", "MOCK_LWM2M_BIND_HOST", "0.0.0.0"),
                Integer.parseInt(MockDeviceConfig.read("mock.lwm2m.bindPort", "MOCK_LWM2M_BIND_PORT", "5683")),
                MockDeviceConfig.read("mock.lwm2m.brokerUrl", "MOCK_LWM2M_BROKER_URL", "tcp://localhost:1883"),
                MockDeviceConfig.read("mock.lwm2m.mqttClientId", "MOCK_LWM2M_MQTT_CLIENT_ID", "lwm2m-gateway-" + gatewayId),
                Integer.parseInt(MockDeviceConfig.read("mock.lwm2m.mqttQos", "MOCK_LWM2M_MQTT_QOS", "1")),
                MockDeviceConfig.read("mock.lwm2m.topicPattern", "MOCK_LWM2M_TOPIC_PATTERN", "iot/{deviceId}/telemetry"),
                MockDeviceConfig.read("mock.lwm2m.commandTopicFilter", "MOCK_LWM2M_COMMAND_TOPIC_FILTER", "iot/device/+/commands"),
                MockDeviceConfig.read("mock.lwm2m.commandAckTopicPattern", "MOCK_LWM2M_COMMAND_ACK_TOPIC_PATTERN", "iot/device/{deviceId}/command-acks"),
                MockDeviceConfig.read("mock.lwm2m.commandConsumerClientId", "MOCK_LWM2M_COMMAND_CONSUMER_CLIENT_ID", "lwm2m-gateway-command-" + gatewayId));
    }
}
