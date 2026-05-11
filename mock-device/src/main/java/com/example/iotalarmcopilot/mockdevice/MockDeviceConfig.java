package com.example.iotalarmcopilot.mockdevice;

public record MockDeviceConfig(
        String brokerUrl,
        String clientId,
        String deviceId,
        String topic,
        int qos,
        long intervalMs,
        int maxMessages) {

    public static MockDeviceConfig load() {
        String deviceId = read("mock.deviceId", "MOCK_DEVICE_ID", "demo-001");
        return new MockDeviceConfig(
                read("mock.brokerUrl", "MOCK_BROKER_URL", "tcp://localhost:1883"),
                read("mock.clientId", "MOCK_CLIENT_ID", "mock-device-" + deviceId),
                deviceId,
                read("mock.topic", "MOCK_TOPIC", "iot/" + deviceId + "/telemetry"),
                Integer.parseInt(read("mock.qos", "MOCK_QOS", "1")),
                Long.parseLong(read("mock.intervalMs", "MOCK_INTERVAL_MS", "5000")),
                Integer.parseInt(read("mock.maxMessages", "MOCK_MAX_MESSAGES", "0")));
    }

    private static String read(String propertyKey, String envKey, String defaultValue) {
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
