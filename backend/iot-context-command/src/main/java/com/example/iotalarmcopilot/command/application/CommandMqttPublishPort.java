package com.example.iotalarmcopilot.command.application;

/**
 * 命令MQTT发布接口
 */
public interface CommandMqttPublishPort {

    void publish(String deviceId, String payloadJson);
}
