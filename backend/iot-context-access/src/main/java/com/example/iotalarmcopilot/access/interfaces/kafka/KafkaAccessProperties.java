package com.example.iotalarmcopilot.access.interfaces.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "iot.kafka")
public class KafkaAccessProperties {

    private boolean enabled = true;
    private String telemetryTopic = "iot.telemetry.raw";
    private String consumerGroup = "iot-platform-access";
}
