package com.example.iotalarmcopilot.command.infrastructure.persistence;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.command.domain.model.DeviceCommand;
import com.example.iotalarmcopilot.command.domain.model.CommandStatus;
import com.example.iotalarmcopilot.command.domain.repository.DeviceCommandRepository;
import com.example.iotalarmcopilot.command.domain.repository.DeviceCommandSaveResult;
import com.example.iotalarmcopilot.command.domain.repository.DeviceCommandStatusUpdateResult;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 命令实体存储
 */
@Repository
public class MybatisDeviceCommandRepository implements DeviceCommandRepository {

    private final DeviceCommandMapper deviceCommandMapper;

    public MybatisDeviceCommandRepository(DeviceCommandMapper deviceCommandMapper) {
        this.deviceCommandMapper = deviceCommandMapper;
    }

    @Override
    public DeviceCommandSaveResult saveIfAbsent(DeviceCommand command) {
        DeviceCommandRecord record = DeviceCommandRecord.fromDomain(command);
        int insertedRows = deviceCommandMapper.insertIgnore(record);
        DeviceCommandRecord savedRecord = deviceCommandMapper.selectByCommandId(command.commandId());
        if (savedRecord == null) {
            throw new BaseDomainException("Failed to persist or load device command. commandId=" + command.commandId());
        }
        return new DeviceCommandSaveResult(savedRecord.toDomain(), insertedRows == 1);
    }

    @Override
    public DeviceCommand loadByCommandId(String commandId) {
        DeviceCommandRecord record = deviceCommandMapper.selectByCommandId(commandId);
        if (record == null) {
            throw new BaseDomainException("Device command not found. commandId=" + commandId);
        }
        return record.toDomain();
    }

    @Override
    public Optional<DeviceCommand> findByCommandId(String commandId) {
        return Optional.ofNullable(deviceCommandMapper.selectByCommandId(commandId))
                .map(DeviceCommandRecord::toDomain);
    }

    @Override
    public DeviceCommandStatusUpdateResult updateStatus(DeviceCommand command) {
        int updatedRows = deviceCommandMapper.updateStatus(DeviceCommandRecord.fromDomain(command));
        DeviceCommand latest = loadByCommandId(command.commandId());
        return new DeviceCommandStatusUpdateResult(latest, updatedRows == 1);
    }

    @Override
    public DeviceCommandStatusUpdateResult updateStatusIfCurrentStatus(DeviceCommand command, CommandStatus currentStatus) {
        int updatedRows = deviceCommandMapper.updateStatusIfCurrentStatus(
                DeviceCommandRecord.fromDomain(command),
                currentStatus.name());
        DeviceCommand latest = loadByCommandId(command.commandId());
        return new DeviceCommandStatusUpdateResult(latest, updatedRows == 1);
    }

    @Override
    public List<DeviceCommand> findTimedOutCandidates(Instant sentBefore, int limit) {
        return deviceCommandMapper.selectTimedOutCandidates(sentBefore, limit).stream()
                .map(DeviceCommandRecord::toDomain)
                .toList();
    }

    @Override
    public List<DeviceCommand> recent(int limit) {
        return deviceCommandMapper.selectRecent(limit).stream()
                .map(DeviceCommandRecord::toDomain)
                .toList();
    }
}
