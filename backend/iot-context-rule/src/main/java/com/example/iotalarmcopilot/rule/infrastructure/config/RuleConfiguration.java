package com.example.iotalarmcopilot.rule.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RuleProperties.class)
public class RuleConfiguration {
}
