package com.example.iotalarmcopilot.command.infrastructure;

import com.example.iotalarmcopilot.command.application.CommandTimeoutProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启用定时任务配置
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(CommandTimeoutProperties.class)
public class CommandSchedulingConfiguration {
}
