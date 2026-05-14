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

    public TelemetryEvent record(RecordTelemetryCommand command) {
        TelemetryEvent event = TelemetryEvent.record(
                command.deviceId(),
                command.metrics(),
                command.reportedAt(),
                command.rawJson());
        TelemetryEvent savedEvent = telemetryEventRepository.save(event);
        applicationEventPublisher.publishEvent(new TelemetryRecordedEvent(
                savedEvent.id(),
                savedEvent.deviceId().value(),
                savedEvent.metrics(),
                savedEvent.reportedAt()));
        return savedEvent;
    }
}
