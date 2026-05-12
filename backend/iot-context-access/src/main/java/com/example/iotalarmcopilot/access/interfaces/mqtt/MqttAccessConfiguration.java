package com.example.iotalarmcopilot.access.interfaces.mqtt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MqttAccessProperties.class)
public class MqttAccessConfiguration {
}
