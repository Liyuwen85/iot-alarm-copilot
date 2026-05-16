package com.example.iotalarmcopilot.telemetry.application;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 遥测查询应用服务
 */
@Service
public class TelemetryQueryApplicationService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final TelemetryHotDataPort telemetryHotDataPort;

    public TelemetryQueryApplicationService(TelemetryHotDataPort telemetryHotDataPort) {
        this.telemetryHotDataPort = telemetryHotDataPort;
    }

    public List<TelemetryEventVO> recent(int limit) {
        int safeLimit = normalizeLimit(limit);
        return telemetryHotDataPort.recent(safeLimit);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
