package com.example.iotalarmcopilot.access.interfaces.mqtt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "iot.mqtt")
public class MqttAccessProperties {

    private boolean enabled = true;
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId = "iot-platform-backend";
    private String inboundTopic = "iot/+/telemetry";
    private int qos = 1;

}
