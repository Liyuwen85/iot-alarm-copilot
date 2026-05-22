package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.access.infrastructure.persistence.AccessDeadLetterMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 死信查询服务
 */
@Service
public class AccessDeadLetterQueryApplicationService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final AccessDeadLetterMapper accessDeadLetterMapper;

    public AccessDeadLetterQueryApplicationService(AccessDeadLetterMapper accessDeadLetterMapper) {
        this.accessDeadLetterMapper = accessDeadLetterMapper;
    }

    public List<AccessDeadLetterVO> recent(int limit) {
        int safeLimit = normalizeLimit(limit);
        return accessDeadLetterMapper.selectRecent(safeLimit).stream()
                .map(record -> new AccessDeadLetterVO(
                        record.getId(),
                        record.getDeadLetterTopic(),
                        record.getOriginalTopic(),
                        record.getOriginalPartition(),
                        record.getOriginalOffset(),
                        record.getConsumerGroup(),
                        record.getMqttTopic(),
                        record.getDeviceId(),
                        record.getPayload(),
                        record.getExceptionType(),
                        record.getExceptionMessage(),
                        record.getFailedAt(),
                        record.getCreatedAt()))
                .toList();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
