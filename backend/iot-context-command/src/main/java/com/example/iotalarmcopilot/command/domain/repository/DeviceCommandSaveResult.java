package com.example.iotalarmcopilot.command.domain.repository;

import com.example.iotalarmcopilot.command.domain.model.DeviceCommand;

/**
 * 命令保存结果
 */
public record DeviceCommandSaveResult(
        DeviceCommand command,
        boolean created) {
}
