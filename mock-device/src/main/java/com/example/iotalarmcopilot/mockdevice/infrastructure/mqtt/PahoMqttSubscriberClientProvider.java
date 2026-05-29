package com.example.iotalarmcopilot.mockdevice.infrastructure.mqtt;

import com.example.iotalarmcopilot.mockdevice.interfaces.mqtt.MqttSubscriberClientProvider;
import org.eclipse.paho.client.mqttv3.MqttClient;

public record PahoMqttSubscriberClientProvider(
        String brokerUrl,
        String clientId,
        String commandTopic,
        int qos) implements MqttSubscriberClientProvider {

    @Override
    public MqttClient create() {
        return PahoMqttClientFactory.create(brokerUrl, clientId);
    }

    @Override
    public void close(MqttClient client) {
        PahoMqttClientFactory.close(client);
    }

}
