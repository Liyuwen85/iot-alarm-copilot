package com.example.iotalarmcopilot.command.infrastructure.mqtt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CommandMqttProperties.class)
public class CommandMqttConfiguration {
}
