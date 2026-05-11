package com.example.iotalarmcopilot.mockdevice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单模拟设备发送数据
 */
public final class MockDeviceApplication {

    private MockDeviceApplication() {
    }

    public static void main(String[] args) {
        MockDeviceConfig config = MockDeviceConfig.load();

        ObjectMapper objectMapper = new ObjectMapper();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // 结束标志
        AtomicBoolean finished = new AtomicBoolean(false);
        CountDownLatch completion = new CountDownLatch(1);
        // 发布次数
        AtomicInteger publishedCount = new AtomicInteger(0);

        try (MqttClient client = new MqttClient(
                config.brokerUrl(),
                config.clientId(),
                new MemoryPersistence())) {

            connect(client);

            System.out.printf(
                    "mock-device connected broker=%s clientId=%s topic=%s intervalMs=%d maxMessages=%d%n",
                    config.brokerUrl(),
                    config.clientId(),
                    config.topic(),
                    config.intervalMs(),
                    config.maxMessages());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                stopScheduler(scheduler);
                disconnectQuietly(client);
            }));

            scheduler.scheduleAtFixedRate(
                    () -> publishOnce(client, objectMapper, config, publishedCount, finished, completion),
                    0,
                    config.intervalMs(),
                    TimeUnit.MILLISECONDS);

            completion.await();
        } catch (Exception exception) {
            throw new IllegalStateException("mock-device failed to run", exception);
        } finally {
            stopScheduler(scheduler);
        }
    }

    private static void connect(MqttClient client) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);
    }

    private static void publishOnce(
            MqttClient client,
            ObjectMapper objectMapper,
            MockDeviceConfig config,
            AtomicInteger publishedCount,
            AtomicBoolean finished,
            CountDownLatch completion) {
        if (finished.get()) {
            return;
        }

        int sequence = publishedCount.incrementAndGet();
        try {
            if (!client.isConnected()) {
                System.out.println("mock-device waiting for MQTT reconnect");
                publishedCount.decrementAndGet();
                return;
            }

            MockTelemetryPayload payload = nextPayload(config.deviceId(), sequence);
            String payloadJson = objectMapper.writeValueAsString(payload);
            MqttMessage message = new MqttMessage(payloadJson.getBytes(StandardCharsets.UTF_8));
            message.setQos(config.qos());
            client.publish(config.topic(), message);

            System.out.printf("published #%d topic=%s payload=%s%n", sequence, config.topic(), payloadJson);

            if (config.maxMessages() > 0 && sequence >= config.maxMessages()) {
                finished.set(true);
                completion.countDown();
            }
        } catch (JsonProcessingException exception) {
            finished.set(true);
            completion.countDown();
            throw new IllegalStateException("failed to serialize telemetry payload", exception);
        } catch (MqttException exception) {
            publishedCount.decrementAndGet();
            System.out.printf("publish failed topic=%s reason=%s%n", config.topic(), exception.getMessage());
        }
    }

    private static MockTelemetryPayload nextPayload(String deviceId, int sequence) {
        Random random = new Random(sequence * 97L + 13L);
        BigDecimal temperature = toDecimal(sequence % 5 == 0
                ? 80 + (random.nextDouble() * 5)
                : 72 + (random.nextDouble() * 6));
        BigDecimal humidity = toDecimal(35 + (random.nextDouble() * 15));
        return new MockTelemetryPayload(
                deviceId,
                temperature,
                humidity,
                OffsetDateTime.now().toString());
    }

    private static BigDecimal toDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static void stopScheduler(ScheduledExecutorService scheduler) {
        scheduler.shutdownNow();
    }

    private static void disconnectQuietly(MqttClient client) {
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException exception) {
            System.out.printf("disconnect failed reason=%s%n", exception.getMessage());
        }
    }
}
