package com.example.iotalarmcopilot.mockdevice.application;

import com.example.iotalarmcopilot.mockdevice.application.port.MqttMessagePublisher;
import com.example.iotalarmcopilot.mockdevice.config.MockDeviceConfig;
import com.example.iotalarmcopilot.mockdevice.domain.CommandAckPayload;
import com.example.iotalarmcopilot.mockdevice.domain.MockTelemetryPayload;
import com.example.iotalarmcopilot.mockdevice.domain.SetReportIntervalCommandPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 模拟设备遥测上报和下行处理
 */
public class MockDeviceTelemetryService {

    private final MockDeviceConfig config;
    private final MqttMessagePublisher mqttMessagePublisher;

    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean finished;
    private final AtomicInteger publishedCount;
    private final AtomicLong currentIntervalMs;
    private final AtomicLong lastPublishedAtMs;
    private final CountDownLatch completion;
    private final ObjectMapper objectMapper;

    public MockDeviceTelemetryService(MockDeviceConfig config, MqttMessagePublisher mqttMessagePublisher) {
        this.config = config;
        this.mqttMessagePublisher = mqttMessagePublisher;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.finished = new AtomicBoolean(false);
        this.publishedCount = new AtomicInteger(0);
        this.currentIntervalMs = new AtomicLong(config.intervalMs());
        this.lastPublishedAtMs = new AtomicLong(0);
        this.completion = new CountDownLatch(1);
        this.objectMapper = new ObjectMapper();
    }

    public void start() {
        System.out.printf("mock-device telemetry scheduler starting intervalMs=%d maxMessages=%d%n",
                config.intervalMs(),
                config.maxMessages());
        scheduler.scheduleAtFixedRate(
                this::publishOnce,
                0,
                200,
                TimeUnit.MILLISECONDS);
    }

    public void awaitCompletion() {
        try {
            completion.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("mqtt simulator interrupted", exception);
        } finally {
            stop();
        }
    }

    public void stop() {
        finished.set(true);
        completion.countDown();
        scheduler.shutdownNow();
    }

    public void processSetReportIntervalCommandPayload(SetReportIntervalCommandPayload command) {
        if (!"set_report_interval".equalsIgnoreCase(command.commandType())) {
            publishAck(new CommandAckPayload(
                    command.commandId(),
                    config.deviceId(),
                    "FAILED",
                    OffsetDateTime.now().toString(),
                    "unsupported command type"));
            return;
        }
        if (command.params() == null || command.params().intervalMs() < 500) {
            publishAck(new CommandAckPayload(
                    command.commandId(),
                    config.deviceId(),
                    "FAILED",
                    OffsetDateTime.now().toString(),
                    "invalid interval"));
            return;
        }

        currentIntervalMs.set(command.params().intervalMs());

        publishAck(new CommandAckPayload(
                command.commandId(),
                config.deviceId(),
                "SUCCESS",
                OffsetDateTime.now().toString(),
                "interval changed to " + command.params().intervalMs()));
        System.out.printf("command-applied source=device deviceId=%s ackTopic=%s commandId=%s intervalMs=%d%n",
                config.deviceId(),
                config.commandAckTopic(),
                command.commandId(),
                command.params().intervalMs());
    }

    private void publishOnce() {
        if (finished.get()) {
            return;
        }
        if (!shouldPublishNow()) {
            return;
        }

        int sequence = publishedCount.incrementAndGet();

        try {
            MockTelemetryPayload payload = nextPayload(config.deviceId(), sequence);
            String payloadJson = objectMapper.writeValueAsString(payload);
            System.out.printf("mock-device telemetry publishing topic=%s sequence=%d payload=%s%n",
                    config.telemetryTopic(),
                    sequence,
                    payloadJson);

            mqttMessagePublisher.publish(config.telemetryTopic(), payloadJson, config.qos());

            lastPublishedAtMs.set(System.currentTimeMillis());
            System.out.printf("mock-device telemetry published topic=%s sequence=%d%n",
                    config.telemetryTopic(),
                    sequence);

            if (config.maxMessages() > 0 && sequence >= config.maxMessages()) {
                finished.set(true);
                completion.countDown();
            }
        } catch (JsonProcessingException exception) {
            finished.set(true);
            completion.countDown();
            throw new IllegalStateException("failed to serialize telemetry payload", exception);
        } catch (RuntimeException exception) {
            System.out.printf("mock-device telemetry publish failed topic=%s sequence=%d reason=%s%n",
                    config.telemetryTopic(),
                    sequence,
                    exception.getMessage());
            throw exception;
        }
    }

    private boolean shouldPublishNow() {
        long intervalMs = currentIntervalMs.get();
        long lastPublishedAt = lastPublishedAtMs.get();
        if (lastPublishedAt == 0L) {
            return true;
        }
        return System.currentTimeMillis() - lastPublishedAt >= intervalMs;
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

    private void publishAck(CommandAckPayload ackPayload) {
        try {
            String ackJson = objectMapper.writeValueAsString(ackPayload);
            mqttMessagePublisher.publish(config.commandAckTopic(), ackJson, config.qos());
            System.out.printf("command-ack-published source=device topic=%s payload=%s%n",
                    config.commandAckTopic(),
                    ackJson);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize ack payload", exception);
        }
    }
}
