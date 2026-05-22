package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.telemetry.application.TelemetryEventVO;
import com.example.iotalarmcopilot.telemetry.application.TelemetryHotDataPort;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 热点数据存储（关系型存储）实现，仅用于测试
 */
@Component
@ConditionalOnMissingBean(TelemetryHotDataPort.class)
public class MybatisTelemetryHotDataPort implements TelemetryHotDataPort {

    private final TelemetryEventMapper telemetryEventMapper;

    public MybatisTelemetryHotDataPort(TelemetryEventMapper telemetryEventMapper) {
        this.telemetryEventMapper = telemetryEventMapper;
    }

    @Override
    public void append(TelemetryEvent event) {
        try {
            TelemetryEventRecord record = TelemetryEventRecord.fromDomain(event);
            int insertedRows = telemetryEventMapper.insert(record);
            if (insertedRows != 1) {
                throw new BaseDomainException("Failed to persist telemetry event");
            }
        } catch (DuplicateKeyException ignored) {
            // Keep repeated ingest idempotent in local fallback mode.
        }
    }

    @Override
    public List<TelemetryEventVO> recent(int limit) {
        return telemetryEventMapper.selectRecent(limit).stream()
                .map(record -> new TelemetryEventVO(
                        record.getId(),
                        record.getDeviceId(),
                        record.getTemperature(),
                        record.getHumidity(),
                        record.getMetricsJson(),
                        record.getReportedAt(),
                        record.getRawJson()))
                .toList();
    }
}
