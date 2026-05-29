package com.example.iotalarmcopilot.mockdevice.interfaces.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;

public interface MqttSubscriberClientProvider {

    MqttClient create();

    void close(MqttClient client);

    String commandTopic();

    default int qos() {
        return 1;
    }

}
