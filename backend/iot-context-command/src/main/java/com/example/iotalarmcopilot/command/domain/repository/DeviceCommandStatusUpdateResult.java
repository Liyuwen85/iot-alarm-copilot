package com.example.iotalarmcopilot.command.domain.repository;

import com.example.iotalarmcopilot.command.domain.model.DeviceCommand;

/**
 * 命令状态更新结果
 */
public record DeviceCommandStatusUpdateResult(
        DeviceCommand command,
        boolean changed) {
}
