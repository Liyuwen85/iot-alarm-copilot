package com.example.iotalarmcopilot.command.interfaces;

import com.example.iotalarmcopilot.command.application.CommandTimeoutProperties;
import com.example.iotalarmcopilot.command.application.CommandTimeoutService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * ”命令超时“定时任务
 */
@Component
public class CommandTimeoutScheduler {

    private final CommandTimeoutService commandTimeoutService;
    private final CommandTimeoutProperties properties;

    public CommandTimeoutScheduler(
            CommandTimeoutService commandTimeoutService,
            CommandTimeoutProperties properties) {
        this.commandTimeoutService = commandTimeoutService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${iot.command.timeout.scan-interval-ms:5000}")
    public void scanTimedOutCommands() {
        if (!properties.isEnabled()) {
            return;
        }
        // 标记为超时
        commandTimeoutService.markTimedOutCommands(
                Duration.ofSeconds(properties.getThresholdSeconds()),
                Instant.now(),
                100);
    }
}
