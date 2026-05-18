package com.example.iotalarmcopilot.bridge;

/**
 * 配置参数
 * @param mqttBrokerUrl
 * @param mqttClientId
 * @param mqttTopicFilter
 * @param mqttQos
 * @param kafkaBootstrapServers
 * @param kafkaTopic
 */
public record BridgeConfig(
        String mqttBrokerUrl,
        String mqttClientId,
        String mqttTopicFilter,
        int mqttQos,
        String kafkaBootstrapServers,
        String kafkaTopic) {

    public static BridgeConfig load() {
        return new BridgeConfig(
                read("bridge.mqttBrokerUrl", "BRIDGE_MQTT_BROKER_URL", "tcp://localhost:1883"),
                read("bridge.mqttClientId", "BRIDGE_MQTT_CLIENT_ID", "mqtt-kafka-bridge"),
                read("bridge.mqttTopicFilter", "BRIDGE_MQTT_TOPIC_FILTER", "iot/+/telemetry"),
                Integer.parseInt(read("bridge.mqttQos", "BRIDGE_MQTT_QOS", "1")),
                read("bridge.kafkaBootstrapServers", "BRIDGE_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
                read("bridge.kafkaTopic", "BRIDGE_KAFKA_TOPIC", "iot.telemetry.raw"));
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
