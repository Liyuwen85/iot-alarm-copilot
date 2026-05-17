package com.example.iotalarmcopilot.alarm.application;

import com.example.iotalarmcopilot.alarm.infrastructure.persistence.AlarmEventMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 告警查询服务
 */
@Service
public class AlarmQueryApplicationService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final AlarmEventMapper alarmEventMapper;

    public AlarmQueryApplicationService(AlarmEventMapper alarmEventMapper) {
        this.alarmEventMapper = alarmEventMapper;
    }

    public List<AlarmVO> recent(int limit) {
        int safeLimit = normalizeLimit(limit);
        return alarmEventMapper.selectRecent(safeLimit).stream()
                .map(record -> new AlarmVO(
                        record.getId(),
                        record.getDedupKey(),
                        record.getRuleCode(),
                        record.getTelemetryEventId(),
                        record.getDeviceId(),
                        record.getMetricName(),
                        record.getMetricValue(),
                        record.getThresholdValue(),
                        record.getSeverity(),
                        record.getStatus(),
                        record.getTriggeredAt(),
                        record.getAcknowledgedAt(),
                        record.getClosedAt()))
                .toList();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
