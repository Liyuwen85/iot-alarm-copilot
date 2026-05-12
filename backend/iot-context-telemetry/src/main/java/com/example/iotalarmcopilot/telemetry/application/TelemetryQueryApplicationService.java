package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.telemetry.infrastructure.persistence.TelemetryEventMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 遥测查询应用服务
 */
@Service
public class TelemetryQueryApplicationService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final TelemetryEventMapper telemetryEventMapper;

    public TelemetryQueryApplicationService(TelemetryEventMapper telemetryEventMapper) {
        this.telemetryEventMapper = telemetryEventMapper;
    }

    /**
     * 最近几条遥测数据
     * @param limit
     * @return
     */
    public List<TelemetryEventVO> recent(int limit) {
        int safeLimit = normalizeLimit(limit);
        return telemetryEventMapper.selectRecent(safeLimit).stream()
                .map(record -> new TelemetryEventVO(
                        record.getId(),
                        record.getDeviceId(),
                        record.getTemperature(),
                        record.getHumidity(),
                        record.getReportedAt(),
                        record.getRawJson()))
                .toList();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
