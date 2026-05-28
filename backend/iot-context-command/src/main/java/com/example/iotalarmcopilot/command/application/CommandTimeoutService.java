package com.example.iotalarmcopilot.command.application;

import com.example.iotalarmcopilot.command.domain.model.DeviceCommand;
import com.example.iotalarmcopilot.command.domain.repository.DeviceCommandRepository;
import com.example.iotalarmcopilot.command.domain.repository.DeviceCommandStatusUpdateResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 命令超时服务
 */
@Service
public class CommandTimeoutService {

    private final DeviceCommandRepository deviceCommandRepository;

    public CommandTimeoutService(DeviceCommandRepository deviceCommandRepository) {
        this.deviceCommandRepository = deviceCommandRepository;
    }

    /**
     * 批量处理超时命令
     *
     * @param timeout
     * @param now
     * @param limit
     * @return
     */
    @Transactional
    public int markTimedOutCommands(Duration timeout, Instant now, int limit) {
        List<DeviceCommand> expiredCommands = deviceCommandRepository.findTimedOutCandidates(now.minus(timeout), limit);
        int changedCount = 0;
        for (DeviceCommand command : expiredCommands) {
            DeviceCommandStatusUpdateResult result = deviceCommandRepository
                    .updateStatusIfCurrentStatus(
                            command.markTimedOut("command ack timeout", now),
                            command.status());
            if (result.changed()) {
                changedCount++;
            }
        }
        return changedCount;
    }
}
