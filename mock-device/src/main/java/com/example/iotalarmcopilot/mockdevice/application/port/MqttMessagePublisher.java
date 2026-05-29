package com.example.iotalarmcopilot.mockdevice.application.port;

/**
 * MQTT消息发布接口
 */
public interface MqttMessagePublisher {

    void publish(String topic, String payload, int qos);

}
