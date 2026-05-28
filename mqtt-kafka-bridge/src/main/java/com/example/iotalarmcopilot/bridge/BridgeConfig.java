package com.example.iotalarmcopilot.bridge;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 配置参数
 */
public record BridgeConfig(
        String mqttBrokerUrl,
        String mqttClientId,
        String mqttTopicFilter,
        int mqttQos,
        String kafkaBootstrapServers,
        String telemetryKafkaTopic,
        String commandAckKafkaTopic,
        boolean leaderElectionEnabled,
        String leaderJdbcUrl,
        String leaderJdbcUsername,
        String leaderJdbcPassword,
        long leaderLockKey,
        long leaderRetryIntervalMs,
        long leaderHealthCheckIntervalMs) {

    public static BridgeConfig load() {
        return new BridgeConfig(
                read("bridge.mqttBrokerUrl", "BRIDGE_MQTT_BROKER_URL", "tcp://localhost:1883"),
                read("bridge.mqttClientId", "BRIDGE_MQTT_CLIENT_ID", defaultMqttClientId()),
                read("bridge.mqttTopicFilter", "BRIDGE_MQTT_TOPIC_FILTER", "iot/+/telemetry,iot/device/+/command-acks"),
                Integer.parseInt(read("bridge.mqttQos", "BRIDGE_MQTT_QOS", "1")),
                read("bridge.kafkaBootstrapServers", "BRIDGE_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
                read("bridge.telemetryKafkaTopic", "BRIDGE_TELEMETRY_KAFKA_TOPIC",
                        read("bridge.kafkaTopic", "BRIDGE_KAFKA_TOPIC", "iot.telemetry.raw")),
                read("bridge.commandAckKafkaTopic", "BRIDGE_COMMAND_ACK_KAFKA_TOPIC", "iot.command.ack.raw"),
                Boolean.parseBoolean(read("bridge.leaderElectionEnabled", "BRIDGE_LEADER_ELECTION_ENABLED", "true")),
                read("bridge.leaderJdbcUrl", "BRIDGE_LEADER_JDBC_URL", "jdbc:postgresql://localhost:5432/iot_alarm_copilot"),
                read("bridge.leaderJdbcUsername", "BRIDGE_LEADER_JDBC_USERNAME", "postgres"),
                read("bridge.leaderJdbcPassword", "BRIDGE_LEADER_JDBC_PASSWORD", "postgres"),
                Long.parseLong(read("bridge.leaderLockKey", "BRIDGE_LEADER_LOCK_KEY", "20260518001")),
                Long.parseLong(read("bridge.leaderRetryIntervalMs", "BRIDGE_LEADER_RETRY_INTERVAL_MS", "3000")),
                Long.parseLong(read("bridge.leaderHealthCheckIntervalMs", "BRIDGE_LEADER_HEALTH_CHECK_INTERVAL_MS", "5000")));
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

    private static String defaultMqttClientId() {
        try {
            return "mqtt-kafka-bridge-" + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {
            return "mqtt-kafka-bridge";
        }
    }
}
