package com.example.iotalarmcopilot.mockdevice.infrastructure.mqtt;

import com.example.iotalarmcopilot.mockdevice.application.port.MqttMessagePublisher;
import org.slf4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

public class PahoMqttMessagePublisher implements MqttMessagePublisher, AutoCloseable {

    private final MqttClient client;
    private final String brokerUrl;
    private final Logger logger;

    public PahoMqttMessagePublisher(String brokerUrl, String clientId, Logger logger) {
        this.client = PahoMqttClientFactory.create(brokerUrl, clientId);
        this.brokerUrl = brokerUrl;
        this.logger = logger;
    }

    @Override
    public void publish(String topic, String payload, int qos) {
        if (!ensureConnected()) {
            return;
        }

        try {
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(qos);
            client.publish(topic, message);
        } catch (Exception e) {
            throw new IllegalStateException("failed to publish mqtt message", e);
        }
    }

    @Override
    public void close() {
        PahoMqttClientFactory.close(client);
    }

    private boolean ensureConnected() {
        if (client == null) {
            return false;
        }
        if (client.isConnected()) {
            return true;
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        try {
            client.connect(options);
            return true;
        } catch (MqttException exception) {
            logger.warn("mqtt publisher waiting broker={} reason={}",
                    this.brokerUrl,
                    exception.getMessage());
            return false;
        }
    }
}
