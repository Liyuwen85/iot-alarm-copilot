package com.example.iotalarmcopilot.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Broker --> Kafka (--> Backend)
 * 没找到Mosquitto官方的kafka支持，就写了此工程（生产环境可用EMQX，就不需要此工程）
 */
public final class MqttKafkaBridgeApplication {

    // format: iot/deviceId/telemetry
    private static final String TOPIC_PREFIX = "iot";
    private static final String TOPIC_SUFFIX = "telemetry";

    private MqttKafkaBridgeApplication() {
    }

    public static void main(String[] args) throws Exception {
        BridgeConfig config = BridgeConfig.load();
        ObjectMapper objectMapper = new ObjectMapper();

        while (true) {
            if (!config.leaderElectionEnabled()) {
                runBridge(config, objectMapper);
                return;
            }

            // 使用postgresql来做HA
            // leader选举
            try (PostgresLeaderElector leaderElector = new PostgresLeaderElector(config)) {
                boolean acquired = leaderElector.tryAcquire();
                if (!acquired) {
                    System.out.printf("bridge standby waiting for leader lock key=%s retryMs=%s%n",
                            config.leaderLockKey(),
                            config.leaderRetryIntervalMs());
                    Thread.sleep(config.leaderRetryIntervalMs());
                    continue;
                }
                System.out.printf("bridge became leader lockKey=%s jdbcUrl=%s%n",
                        config.leaderLockKey(),
                        config.leaderJdbcUrl());
                runBridge(config, objectMapper, leaderElector);
            } catch (Exception exception) {
                System.out.printf("bridge leader loop error message=%s retryMs=%s%n",
                        exception.getMessage(),
                        config.leaderRetryIntervalMs());
                Thread.sleep(config.leaderRetryIntervalMs());
            }
        }
    }

    private static void runBridge(BridgeConfig config, ObjectMapper objectMapper) throws Exception {
        runBridge(config, objectMapper, null);
    }

    private static void runBridge(
            BridgeConfig config,
            ObjectMapper objectMapper,
            PostgresLeaderElector leaderElector) throws Exception {
        CountDownLatch running = new CountDownLatch(1);

        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers());
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.ACKS_CONFIG, "all");

        // kafka producer，注册到MQTT client
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProperties);
             MqttClient mqttClient = new MqttClient(
                     config.mqttBrokerUrl(),
                     config.mqttClientId(),
                     new MemoryPersistence())) {

            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    try {
                        // 订阅
                        mqttClient.subscribe(config.mqttTopicFilter(), config.mqttQos());
                        System.out.printf("bridge subscribed topicFilter=%s reconnect=%s%n",
                                config.mqttTopicFilter(),
                                reconnect);
                    } catch (MqttException exception) {
                        throw new IllegalStateException("failed to subscribe mqtt topic filter", exception);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    System.out.printf("bridge mqtt connection lost reason=%s%n",
                            cause == null ? "unknown" : cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    String deviceId = extractDeviceId(topic);
                    KafkaTelemetryEnvelope envelope = new KafkaTelemetryEnvelope(topic, payload);
                    String envelopeJson = objectMapper.writeValueAsString(envelope);
                    // 构建kafka的consumer的key,保证deviceId在相同node处理
                    producer.send(new ProducerRecord<>(config.kafkaTopic(), deviceId, envelopeJson)).get();
                    System.out.printf("bridge forwarded mqttTopic=%s deviceId=%s kafkaTopic=%s payload=%s%n",
                            topic,
                            deviceId,
                            config.kafkaTopic(),
                            payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            mqttClient.connect(options);

            System.out.printf("bridge started mqttBroker=%s mqttTopicFilter=%s kafkaBootstrap=%s kafkaTopic=%s%n",
                    config.mqttBrokerUrl(),
                    config.mqttTopicFilter(),
                    config.kafkaBootstrapServers(),
                    config.kafkaTopic());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (mqttClient.isConnected()) {
                        mqttClient.disconnect();
                    }
                } catch (MqttException ignored) {
                }
                running.countDown();
            }));

            // HA 模式
            while (true) {
                if (running.await(config.leaderHealthCheckIntervalMs(), TimeUnit.MILLISECONDS)) {
                    return;
                }
                if (leaderElector != null && !leaderElector.isLockHealthy()) {
                    System.out.printf("bridge leader lock lost lockKey=%s, stop mqtt consume%n", config.leaderLockKey());
                    try {
                        if (mqttClient.isConnected()) {
                            mqttClient.disconnect();
                        }
                    } catch (MqttException ignored) {
                    }
                    return;
                }
            }
        }
    }

    private static String extractDeviceId(String topic) {
        String[] segments = topic.split("/");
        if (segments.length != 3 || !TOPIC_PREFIX.equals(segments[0]) || !TOPIC_SUFFIX.equals(segments[2])) {
            throw new IllegalArgumentException("Unsupported mqtt telemetry topic: " + topic);
        }
        if (segments[1].isBlank()) {
            throw new IllegalArgumentException("Device id in mqtt topic must not be blank");
        }
        return segments[1];
    }
}
