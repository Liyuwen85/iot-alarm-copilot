package com.example.iotalarmcopilot.command.infrastructure.mqtt;

import com.example.iotalarmcopilot.command.application.CommandMqttPublishPort;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * MQTT命令发布者
 */
@Component
public class MqttCommandPublisher implements CommandMqttPublishPort {

    private final CommandMqttProperties properties;

    public MqttCommandPublisher(CommandMqttProperties properties) {
        this.properties = properties;
    }

    /**
     * 发送命令
     *
     * @param deviceId
     * @param payloadJson
     */
    @Override
    public void publish(String deviceId, String payloadJson) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("MQTT command publishing is disabled");
        }
        MqttClient client = null;
        try {
            client = new MqttClient(
                    properties.getBrokerUrl(),
                    properties.getClientId(),
                    new MemoryPersistence());
            // 常见属性
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);

            MqttMessage message = new MqttMessage(payloadJson.getBytes(StandardCharsets.UTF_8));
            message.setQos(properties.getQos());

            // 发送topic
            client.publish(resolveCommandTopic(deviceId), message);
            client.disconnect();
        } catch (MqttException exception) {
            throw new IllegalStateException("Failed to publish device command", exception);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (MqttException ignored) {
                }
            }
        }
    }

    private String resolveCommandTopic(String deviceId) {
        return properties.getCommandTopicPrefix() + "/" + deviceId + "/commands";
    }
}
