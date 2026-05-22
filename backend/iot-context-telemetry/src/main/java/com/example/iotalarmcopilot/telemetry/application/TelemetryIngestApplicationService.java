package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.contract.event.TelemetryRecordedEvent;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.telemetry.application.port.TelemetryEventIdGenerator;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySchema;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshot;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshotRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 接收设备上报的遥测数据
 */
@Service
public class TelemetryIngestApplicationService {

    private final TelemetryEventIdGenerator telemetryEventIdGenerator;
    private final TelemetrySchemaResolver telemetrySchemaResolver;
    private final TelemetryDerivedMetricCalculator telemetryDerivedMetricCalculator;
    private final TelemetryHotDataPort telemetryHotDataPort;
    private final TelemetrySnapshotRepository telemetrySnapshotRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TelemetryIngestApplicationService(
            TelemetryEventIdGenerator telemetryEventIdGenerator,
            TelemetrySchemaResolver telemetrySchemaResolver,
            TelemetryDerivedMetricCalculator telemetryDerivedMetricCalculator,
            TelemetryHotDataPort telemetryHotDataPort,
            TelemetrySnapshotRepository telemetrySnapshotRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.telemetryEventIdGenerator = telemetryEventIdGenerator;
        this.telemetrySchemaResolver = telemetrySchemaResolver;
        this.telemetryDerivedMetricCalculator = telemetryDerivedMetricCalculator;
        this.telemetryHotDataPort = telemetryHotDataPort;
        this.telemetrySnapshotRepository = telemetrySnapshotRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 接收设备上报的遥测数据
     *
     * @param command 命令
     * @return 遥测事件
     */
    @Transactional
    public TelemetryEvent record(RecordTelemetryCommand command) {
        // 返回schema
        TelemetrySchema schema = telemetrySchemaResolver.resolveByDeviceId(command.deviceId());

        // 根据schema解析遥测指标(基础和派生)
        TelemetryMetrics normalizedMetrics = telemetryDerivedMetricCalculator.apply(
                command.metrics(),
                schema.derivedMetricDefinitions());
        schema.validate(normalizedMetrics);

        Long telemetryEventId = telemetryEventIdGenerator.nextId(
                command.deviceId(),
                command.reportedAt(),
                command.rawJson());
        TelemetryEvent event = TelemetryEvent.record(
                telemetryEventId,
                command.deviceId(),
                normalizedMetrics,
                command.reportedAt(),
                command.rawJson());
        // 保存到热点数据
        telemetryHotDataPort.append(event);

        // 刷新快照
        TelemetrySnapshot snapshot = telemetrySnapshotRepository.findByDeviceId(event.deviceId())
                .map(existing -> existing.refreshBy(event))
                .orElseGet(() -> TelemetrySnapshot.capture(event));
        telemetrySnapshotRepository.save(snapshot);

        // 发布遥测事件
        applicationEventPublisher.publishEvent(new TelemetryRecordedEvent(
                event.id(),
                event.deviceId().value(),
                event.metrics(),
                event.reportedAt()));
        return event;
    }
}
