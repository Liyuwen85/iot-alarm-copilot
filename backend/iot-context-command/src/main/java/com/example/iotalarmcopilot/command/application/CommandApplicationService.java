package com.example.iotalarmcopilot.command.application;

import com.example.iotalarmcopilot.command.domain.model.DeviceCommand;
import com.example.iotalarmcopilot.command.domain.repository.DeviceCommandRepository;
import com.example.iotalarmcopilot.command.domain.repository.DeviceCommandSaveResult;
import com.example.iotalarmcopilot.command.domain.repository.DeviceCommandStatusUpdateResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 命令应用服务
 */
@Service
public class CommandApplicationService {

    private final DeviceCommandRepository deviceCommandRepository;
    private final CommandMqttPublishPort commandMqttPublishPort;
    private final ObjectMapper objectMapper;

    public CommandApplicationService(
            DeviceCommandRepository deviceCommandRepository,
            CommandMqttPublishPort commandMqttPublishPort,
            ObjectMapper objectMapper) {
        this.deviceCommandRepository = deviceCommandRepository;
        this.commandMqttPublishPort = commandMqttPublishPort;
        this.objectMapper = objectMapper;
    }

    /**
     * 发送设置上报间隔的命令
     */
    @Transactional
    public DeviceCommandVO sendSetReportInterval(SendSetReportIntervalCommand command) {
        Instant now = command.requestedAt();
        String commandId = "cmd-" + UUID.randomUUID();
        String payloadJson = buildSetReportIntervalPayload(commandId, command.deviceId(), command.intervalMs(), now);
        DeviceCommand created = DeviceCommand.createSetReportInterval(commandId, command.deviceId(), payloadJson, now);
        DeviceCommandSaveResult saveResult = deviceCommandRepository.saveIfAbsent(created);
        DeviceCommand saved = saveResult.command();
        // 直接发给broker
        commandMqttPublishPort.publish(saved.deviceId(), saved.payloadJson());
        // 更新命令状态
        DeviceCommandStatusUpdateResult updateResult = deviceCommandRepository.updateStatus(saved.markSent(now));
        return toVO(updateResult.command());
    }

    public List<DeviceCommandVO> recent(int limit) {
        int safeLimit = limit <= 0 ? 20 : Math.min(limit, 100);
        return deviceCommandRepository.recent(safeLimit).stream()
                .map(this::toVO)
                .toList();
    }

    public DeviceCommandVO toVO(DeviceCommand command) {
        return new DeviceCommandVO(
                command.id(),
                command.commandId(),
                command.deviceId(),
                command.commandType().name(),
                command.payloadJson(),
                command.status().name(),
                command.ackMessage(),
                command.sentAt(),
                command.ackedAt(),
                command.createdAt(),
                command.updatedAt());
    }

    private String buildSetReportIntervalPayload(String commandId, String deviceId, int intervalMs, Instant sentAt) {
        try {
            return objectMapper.writeValueAsString(new SetReportIntervalPayload(
                    commandId,
                    deviceId,
                    "set_report_interval",
                    sentAt,
                    new SetReportIntervalParams(intervalMs)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build command payload", exception);
        }
    }

    private record SetReportIntervalPayload(
            String commandId,
            String deviceId,
            String commandType,
            Instant sentAt,
            SetReportIntervalParams params) {
    }

    private record SetReportIntervalParams(int intervalMs) {
    }
}
