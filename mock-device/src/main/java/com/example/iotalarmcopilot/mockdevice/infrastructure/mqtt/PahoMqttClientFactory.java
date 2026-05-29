package com.example.iotalarmcopilot.mockdevice.infrastructure.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MQTT 客户端工厂
 */
public class PahoMqttClientFactory {

    public static MqttClient create(String brokerUrl, String clientId) {
        MqttClient client = null;
        try {
            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            return client;
        } catch (Exception e) {
            throw new IllegalStateException("failed to create mqtt client", e);
        }
    }

    public static void close(MqttClient client) {
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        } catch (Exception e) {
            throw new IllegalStateException("failed to disconnect mqtt client", e);
        }
    }

}
