package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.access.domain.AccessDeadLetterLog;
import com.example.iotalarmcopilot.access.domain.AccessDeadLetterLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 死信捕获服务
 */
@Service
public class AccessDeadLetterCaptureApplicationService {

    private final AccessDeadLetterLogRepository accessDeadLetterLogRepository;

    public AccessDeadLetterCaptureApplicationService(AccessDeadLetterLogRepository accessDeadLetterLogRepository) {
        this.accessDeadLetterLogRepository = accessDeadLetterLogRepository;
    }

    /**
     * 记录死信
     */
    @Transactional
    public AccessDeadLetterLog record(RecordAccessDeadLetterCommand command) {
        return accessDeadLetterLogRepository.saveIfAbsent(AccessDeadLetterLog.create(
                command.deadLetterTopic(),
                command.originalTopic(),
                command.originalPartition(),
                command.originalOffset(),
                command.consumerGroup(),
                command.mqttTopic(),
                command.deviceId(),
                command.payload(),
                command.exceptionType(),
                command.exceptionMessage(),
                command.failedAt()));
    }
}
