package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.contract.event.TelemetryRecordedEvent;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEventIdGenerator;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshot;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshotRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 遥测数据处理应用服务
 */
@Service
public class TelemetryIngestApplicationService {

    private final TelemetryEventIdGenerator telemetryEventIdGenerator;
    private final TelemetryHotDataPort telemetryHotDataPort;
    private final TelemetrySnapshotRepository telemetrySnapshotRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TelemetryIngestApplicationService(
            TelemetryEventIdGenerator telemetryEventIdGenerator,
            TelemetryHotDataPort telemetryHotDataPort,
            TelemetrySnapshotRepository telemetrySnapshotRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.telemetryEventIdGenerator = telemetryEventIdGenerator;
        this.telemetryHotDataPort = telemetryHotDataPort;
        this.telemetrySnapshotRepository = telemetrySnapshotRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 记录遥测数据
     *
     * @param command
     * @return
     */
    public TelemetryEvent record(RecordTelemetryCommand command) {
        Long telemetryEventId = telemetryEventIdGenerator.nextId(
                command.deviceId(),
                command.reportedAt(),
                command.rawJson());
        TelemetryEvent event = TelemetryEvent.record(
                telemetryEventId,
                command.deviceId(),
                command.metrics(),
                command.reportedAt(),
                command.rawJson());
        // 存储遥测数据(一般在时序库中)
        telemetryHotDataPort.append(event);
        // 最近遥测数据
        TelemetrySnapshot snapshot = telemetrySnapshotRepository.findByDeviceId(event.deviceId())
                // 用最新的数据更新快照
                .map(existing -> existing.refreshBy(event))
                // 没有就创建新的快照
                .orElseGet(() -> TelemetrySnapshot.capture(event));
        // 保存快照(一般在关系库中)
        telemetrySnapshotRepository.save(snapshot);
        // 发送遥测事件
        applicationEventPublisher.publishEvent(new TelemetryRecordedEvent(
                event.id(),
                event.deviceId().value(),
                event.metrics(),
                event.reportedAt()));
        return event;
    }
}
