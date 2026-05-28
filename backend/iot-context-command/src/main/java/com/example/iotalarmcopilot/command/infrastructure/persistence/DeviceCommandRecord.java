package com.example.iotalarmcopilot.command.infrastructure.persistence;

import com.example.iotalarmcopilot.command.domain.model.CommandStatus;
import com.example.iotalarmcopilot.command.domain.model.CommandType;
import com.example.iotalarmcopilot.command.domain.model.DeviceCommand;
import lombok.Data;

import java.time.Instant;

/**
 * 设备命令记录数据库实体
 */
@Data
public class DeviceCommandRecord {

    private Long id;
    private String commandId;
    private String deviceId;
    private String commandType;
    private String payloadJson;
    private String status;
    private String ackMessage;
    private Instant sentAt;
    private Instant ackedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static DeviceCommandRecord fromDomain(DeviceCommand command) {
        DeviceCommandRecord record = new DeviceCommandRecord();
        record.setId(command.id());
        record.setCommandId(command.commandId());
        record.setDeviceId(command.deviceId());
        record.setCommandType(command.commandType().name());
        record.setPayloadJson(command.payloadJson());
        record.setStatus(command.status().name());
        record.setAckMessage(command.ackMessage());
        record.setSentAt(command.sentAt());
        record.setAckedAt(command.ackedAt());
        record.setCreatedAt(command.createdAt());
        record.setUpdatedAt(command.updatedAt());
        return record;
    }

    public DeviceCommand toDomain() {
        return new DeviceCommand(
                id,
                commandId,
                deviceId,
                CommandType.valueOf(commandType),
                payloadJson,
                CommandStatus.valueOf(status),
                ackMessage,
                sentAt,
                ackedAt,
                createdAt,
                updatedAt);
    }
}
