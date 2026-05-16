package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.telemetry.domain.DeviceId;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshot;
import com.example.iotalarmcopilot.telemetry.domain.TelemetrySnapshotRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 快照存储实现
 */
@Repository
public class MybatisTelemetrySnapshotRepository implements TelemetrySnapshotRepository {

    private final TelemetrySnapshotMapper telemetrySnapshotMapper;

    public MybatisTelemetrySnapshotRepository(TelemetrySnapshotMapper telemetrySnapshotMapper) {
        this.telemetrySnapshotMapper = telemetrySnapshotMapper;
    }

    @Override
    public void save(TelemetrySnapshot snapshot) {
        int affectedRows = telemetrySnapshotMapper.upsert(TelemetrySnapshotRecord.fromDomain(snapshot));
        if (affectedRows < 1) {
            throw new BaseDomainException("Failed to persist telemetry snapshot");
        }
    }

    @Override
    public Optional<TelemetrySnapshot> findByDeviceId(DeviceId deviceId) {
        return Optional.ofNullable(telemetrySnapshotMapper.selectByDeviceId(deviceId.value()))
                .map(TelemetrySnapshotRecord::toDomain);
    }
}
