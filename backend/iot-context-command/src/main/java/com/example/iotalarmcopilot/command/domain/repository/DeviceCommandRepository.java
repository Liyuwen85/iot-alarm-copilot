package com.example.iotalarmcopilot.command.domain.repository;

import com.example.iotalarmcopilot.command.domain.model.CommandStatus;
import com.example.iotalarmcopilot.command.domain.model.DeviceCommand;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 设备命令存储接口
 */
public interface DeviceCommandRepository {

    DeviceCommandSaveResult saveIfAbsent(DeviceCommand command);

    DeviceCommand loadByCommandId(String commandId);

    Optional<DeviceCommand> findByCommandId(String commandId);

    DeviceCommandStatusUpdateResult updateStatus(DeviceCommand command);

    DeviceCommandStatusUpdateResult updateStatusIfCurrentStatus(DeviceCommand command, CommandStatus currentStatus);

    // 返回超时列表
    List<DeviceCommand> findTimedOutCandidates(Instant sentBefore, int limit);

    List<DeviceCommand> recent(int limit);
}
