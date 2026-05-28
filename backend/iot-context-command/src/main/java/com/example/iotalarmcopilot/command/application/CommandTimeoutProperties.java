package com.example.iotalarmcopilot.command.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "iot.command.timeout")
public class CommandTimeoutProperties {

    private boolean enabled = true;
    // 命令超时时间(未收到ACK的命令)
    private long thresholdSeconds = 30;
    // 记录超时扫描间隔
    private long scanIntervalMs = 5000;
}
