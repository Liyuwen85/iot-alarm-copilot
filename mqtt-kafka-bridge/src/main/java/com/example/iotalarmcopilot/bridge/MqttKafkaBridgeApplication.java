package com.example.iotalarmcopilot.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 把Mosquitto的MQTT消息放入到Kafka中
 */
public final class MqttKafkaBridgeApplication {

    private static final String TOPIC_PREFIX = "iot";
    private static final String TELEMETRY_SUFFIX = "telemetry";
    private static final String DEVICE_SEGMENT = "device";
    private static final String COMMAND_ACK_SUFFIX = "command-acks";

    private MqttKafkaBridgeApplication() {
    }

    public static void main(String[] args) throws Exception {
        BridgeConfig config = BridgeConfig.load();
        ObjectMapper objectMapper = new ObjectMapper();

        while (true) {
            // 未开启HA，就运行一次
            if (!config.leaderElectionEnabled()) {
                runBridge(config, objectMapper);
                return;
            }

            // 开启HA后
            try (PostgresLeaderElector leaderElector = new PostgresLeaderElector(config)) {
                // 获取锁
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

    /**
     * 运行桥接程序
     */
    private static void runBridge(
            BridgeConfig config,
            ObjectMapper objectMapper,
            PostgresLeaderElector leaderElector) throws Exception {
        CountDownLatch running = new CountDownLatch(1);

        // kafka配置
        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers());
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.ACKS_CONFIG, "all");

        // kafka生产者
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProperties);
             // MQTT客户端
             MqttClient mqttClient = new MqttClient(
                     config.mqttBrokerUrl(),
                     config.mqttClientId(),
                     new MemoryPersistence())) {
            // 回调处理
            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    try {
                        subscribeAll(mqttClient, config);
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
                    // 将MQTT消息转为Kafka的消息
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    String deviceId = extractDeviceId(topic);
                    KafkaTelemetryEnvelope envelope = new KafkaTelemetryEnvelope(topic, payload);
                    String envelopeJson = objectMapper.writeValueAsString(envelope);
                    String kafkaTopic = resolveKafkaTopic(config, topic);
                    producer.send(new ProducerRecord<>(kafkaTopic, deviceId, envelopeJson)).get();
                    System.out.printf("bridge forwarded mqttTopic=%s deviceId=%s kafkaTopic=%s payload=%s%n",
                            topic,
                            deviceId,
                            kafkaTopic,
                            payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            // MQTT客户端配置
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            mqttClient.connect(options);

            System.out.printf(
                    "bridge started mqttBroker=%s mqttTopicFilter=%s kafkaBootstrap=%s telemetryKafkaTopic=%s commandAckKafkaTopic=%s%n",
                    config.mqttBrokerUrl(),
                    config.mqttTopicFilter(),
                    config.kafkaBootstrapServers(),
                    config.telemetryKafkaTopic(),
                    config.commandAckKafkaTopic());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (mqttClient.isConnected()) {
                        mqttClient.disconnect();
                    }
                } catch (MqttException ignored) {
                }
                running.countDown();
            }));

            while (true) {
                if (running.await(config.leaderHealthCheckIntervalMs(), TimeUnit.MILLISECONDS)) {
                    return;
                }
                // 检查锁失败，就退出
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

    /**
     * 订阅MQTT主题
     */
    private static void subscribeAll(MqttClient mqttClient, BridgeConfig config) throws MqttException {
        // 多主题
        for (String topicFilter : config.mqttTopicFilter().split(",")) {
            String normalized = topicFilter.trim();
            if (!normalized.isBlank()) {
                mqttClient.subscribe(normalized, config.mqttQos());
                System.out.printf("bridge subscribed topicFilter=%s%n", normalized);
            }
        }
    }

    /**
     * 从MQTT主题中提取设备ID
     */
    private static String extractDeviceId(String topic) {
        String[] segments = topic.split("/");
        // 上行的遥测数据
        if (segments.length == 3
                && TOPIC_PREFIX.equals(segments[0])
                && TELEMETRY_SUFFIX.equals(segments[2])) {
            if (segments[1].isBlank()) {
                throw new IllegalArgumentException("Device id in mqtt topic must not be blank");
            }
            return segments[1];
        }
        // 上行的ACK
        if (segments.length == 4
                && TOPIC_PREFIX.equals(segments[0])
                && DEVICE_SEGMENT.equals(segments[1])
                && COMMAND_ACK_SUFFIX.equals(segments[3])) {
            if (segments[2].isBlank()) {
                throw new IllegalArgumentException("Device id in mqtt topic must not be blank");
            }
            return segments[2];
        }
        throw new IllegalArgumentException("Unsupported mqtt topic: " + topic);
    }

    private static String resolveKafkaTopic(BridgeConfig config, String topic) {
        if (topic.endsWith("/" + TELEMETRY_SUFFIX)) {
            return config.telemetryKafkaTopic();
        }
        if (topic.endsWith("/" + COMMAND_ACK_SUFFIX)) {
            return config.commandAckKafkaTopic();
        }
        throw new IllegalArgumentException("Unsupported mqtt topic: " + topic);
    }
}
