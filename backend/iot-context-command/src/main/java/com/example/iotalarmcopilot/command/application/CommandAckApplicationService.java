package com.example.iotalarmcopilot.command.application;

import com.example.iotalarmcopilot.command.domain.model.DeviceCommand;
import com.example.iotalarmcopilot.command.domain.repository.DeviceCommandRepository;
import com.example.iotalarmcopilot.command.domain.repository.DeviceCommandStatusUpdateResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 命令确认服务
 */
@Service
public class CommandAckApplicationService {

    private final DeviceCommandRepository deviceCommandRepository;

    public CommandAckApplicationService(DeviceCommandRepository deviceCommandRepository) {
        this.deviceCommandRepository = deviceCommandRepository;
    }

    /**
     * 处理ACK消息
     * @param payload
     */
    @Transactional
    public void handleAck(CommandAckPayload payload) {
        DeviceCommand current = deviceCommandRepository.loadByCommandId(payload.commandId());
        if (current.isAcked()) {
            return;
        }
        // 更新ACK状态
        DeviceCommand updated = isSuccess(payload.status())
                ? current.markAckSuccess(payload.message(), payload.ackedAt())
                : current.markAckFailed(payload.message(), payload.ackedAt());
        DeviceCommandStatusUpdateResult ignored = deviceCommandRepository.updateStatus(updated);
    }

    private boolean isSuccess(String status) {
        return "SUCCESS".equalsIgnoreCase(status);
    }
}
