package com.example.iotalarmcopilot.access.interfaces.kafka;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaAccessProperties.class)
public class KafkaAccessConfiguration {
}
