package com.example.iotalarmcopilot.command.infrastructure.mqtt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MQTT命令属性
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "iot.command.mqtt")
public class CommandMqttProperties {

    private boolean enabled = true;
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId = "iot-platform-command";
    private int qos = 1;
    private String commandTopicPrefix = "iot/device";
}
