package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEventRepository;
import org.springframework.stereotype.Service;

/**
 * 遥测数据处理应用服务
 */
@Service
public class TelemetryIngestApplicationService {

    private final TelemetryEventRepository telemetryEventRepository;

    public TelemetryIngestApplicationService(TelemetryEventRepository telemetryEventRepository) {
        this.telemetryEventRepository = telemetryEventRepository;
    }

    /**
     * 记录遥测数据
     *
     * @param command
     * @return
     */
    public TelemetryEvent record(RecordTelemetryCommand command) {
        TelemetryEvent event = new TelemetryEvent(
                null,
                command.deviceId(),
                command.temperature(),
                command.humidity(),
                command.reportedAt(),
                command.rawJson());
        return telemetryEventRepository.save(event);
    }
}
