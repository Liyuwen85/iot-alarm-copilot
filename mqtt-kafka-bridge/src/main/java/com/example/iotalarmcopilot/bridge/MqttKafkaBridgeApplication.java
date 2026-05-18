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

/**
 * Broker --> Kafka (--> Backend)
 * 没找到Mosquitto官方的kafka支持，就写了此工程（生产环境可用EMQX，就不需要此工程）
 */
public final class MqttKafkaBridgeApplication {

    private MqttKafkaBridgeApplication() {
    }

    public static void main(String[] args) throws Exception {
        BridgeConfig config = BridgeConfig.load();
        ObjectMapper objectMapper = new ObjectMapper();
        CountDownLatch running = new CountDownLatch(1);

        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers());
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.ACKS_CONFIG, "all");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProperties);
             MqttClient mqttClient = new MqttClient(
                     config.mqttBrokerUrl(),
                     config.mqttClientId(),
                     new MemoryPersistence())) {

            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    try {
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
                    KafkaTelemetryEnvelope envelope = new KafkaTelemetryEnvelope(topic, payload);
                    String envelopeJson = objectMapper.writeValueAsString(envelope);
                    producer.send(new ProducerRecord<>(config.kafkaTopic(), topic, envelopeJson)).get();
                    System.out.printf("bridge forwarded mqttTopic=%s kafkaTopic=%s payload=%s%n",
                            topic,
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

            running.await();
        }
    }
}
