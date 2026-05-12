package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.contract.event.TelemetryRecordedEvent;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEventRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 遥测数据处理应用服务
 */
@Service
public class TelemetryIngestApplicationService {

    private final TelemetryEventRepository telemetryEventRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TelemetryIngestApplicationService(
            TelemetryEventRepository telemetryEventRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.telemetryEventRepository = telemetryEventRepository;
        this.applicationEventPublisher = applicationEventPublisher;
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
        TelemetryEvent savedEvent = telemetryEventRepository.save(event);

        // 触发订阅事件
        applicationEventPublisher.publishEvent(new TelemetryRecordedEvent(
                savedEvent.id(),
                savedEvent.deviceId(),
                savedEvent.temperature(),
                savedEvent.humidity(),
                savedEvent.reportedAt()));

        return savedEvent;
    }
}
