package com.example.iotalarmcopilot.mockdevice.config;

/**
 * 模拟设备配置
 * @param brokerUrl
 * @param clientId
 * @param deviceId
 * @param telemetryTopic
 * @param commandTopic
 * @param commandAckTopic
 * @param qos
 * @param intervalMs
 * @param maxMessages
 */
public record MockDeviceConfig(
        String brokerUrl,
        String clientId,
        String deviceId,
        String telemetryTopic,
        String commandTopic,
        String commandAckTopic,
        int qos,
        long intervalMs,
        int maxMessages) {

    public static MockDeviceConfig load() {
        String deviceId = read("mock.deviceId", "MOCK_DEVICE_ID", "demo-001");
        return new MockDeviceConfig(
                read("mock.brokerUrl", "MOCK_BROKER_URL", "tcp://localhost:1883"),
                read("mock.clientId", "MOCK_CLIENT_ID", "mock-device-" + deviceId),
                deviceId,
                read("mock.telemetryTopic", "MOCK_TELEMETRY_TOPIC", "iot/" + deviceId + "/telemetry"),
                read("mock.commandTopic", "MOCK_COMMAND_TOPIC", "iot/device/" + deviceId + "/commands"),
                read("mock.commandAckTopic", "MOCK_COMMAND_ACK_TOPIC", "iot/device/" + deviceId + "/command-acks"),
                Integer.parseInt(read("mock.qos", "MOCK_QOS", "1")),
                Long.parseLong(read("mock.intervalMs", "MOCK_INTERVAL_MS", "5000")),
                Integer.parseInt(read("mock.maxMessages", "MOCK_MAX_MESSAGES", "0")));
    }

    static String read(String propertyKey, String envKey, String defaultValue) {
        String propertyValue = System.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        return defaultValue;
    }
}
