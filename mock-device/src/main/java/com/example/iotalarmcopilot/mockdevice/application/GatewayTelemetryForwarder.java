package com.example.iotalarmcopilot.mockdevice.application;

import com.example.iotalarmcopilot.mockdevice.application.port.Lwm2mServerHandler;
import com.example.iotalarmcopilot.mockdevice.application.port.MqttMessagePublisher;
import com.example.iotalarmcopilot.mockdevice.config.Lwm2mGatewayConfig;
import com.example.iotalarmcopilot.mockdevice.domain.GatewayUplinkMessage;
import com.example.iotalarmcopilot.mockdevice.domain.Lwm2mDeviceSnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GatewayTelemetryForwarder implements Lwm2mServerHandler, AutoCloseable {

    private final Lwm2mGatewayConfig config;
    private final MqttMessagePublisher mqttMessagePublisher;
    private final GatewayTelemetryPublishScheduler publishScheduler;
    private final GatewayTelemetryDeduplicator deduplicator;
    private final Map<String, Lwm2mDeviceSnapshot> latestSnapshots;
    private final ObjectMapper objectMapper;

    public GatewayTelemetryForwarder(
            Lwm2mGatewayConfig config,
            MqttMessagePublisher mqttMessagePublisher,
            GatewayTelemetryPublishScheduler publishScheduler,
            GatewayTelemetryDeduplicator deduplicator) {
        this.config = config;
        this.mqttMessagePublisher = mqttMessagePublisher;
        this.publishScheduler = publishScheduler;
        this.deduplicator = deduplicator;
        this.latestSnapshots = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onClientRegistered(String endpoint) {
        deduplicator.forget(endpoint);
        latestSnapshots.remove(endpoint);
    }

    @Override
    public void onClientUnregistered(String endpoint) {
        publishScheduler.cancel(endpoint);
        deduplicator.forget(endpoint);
        latestSnapshots.remove(endpoint);
    }

    @Override
    public void onTelemetryReported(Lwm2mDeviceSnapshot snapshot) {
        latestSnapshots.put(snapshot.deviceId(), snapshot);

        if (!snapshot.isComplete()) {
            return;
        }
    }

    private void publishLatest(String deviceId) {
        Lwm2mDeviceSnapshot latest = latestSnapshots.get(deviceId);
        if (latest == null || !latest.isComplete()) {
            return;
        }

        GatewayUplinkMessage message = latest.toGatewayMessage(config.gatewayId());
        if (deduplicator.isDuplicate(message)) {
            return;
        }

        String topic = config.topicPattern().replace("{deviceId}", message.deviceId());
        try {
            String payload = objectMapper.writeValueAsString(message);
            mqttMessagePublisher.publish(topic, payload, config.mqttQos());
            deduplicator.markPublished(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize telemetry message", e);
        }
    }

    @Override
    public void close() {
        try {
            publishScheduler.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
