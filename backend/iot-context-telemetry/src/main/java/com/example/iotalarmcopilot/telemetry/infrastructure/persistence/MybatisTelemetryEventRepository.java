package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEventRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisTelemetryEventRepository implements TelemetryEventRepository {

    private final TelemetryEventMapper telemetryEventMapper;

    public MybatisTelemetryEventRepository(TelemetryEventMapper telemetryEventMapper) {
        this.telemetryEventMapper = telemetryEventMapper;
    }

    @Override
    public TelemetryEvent save(TelemetryEvent event) {
        TelemetryEventRecord record = TelemetryEventRecord.fromDomain(event);
        int insertedRows = telemetryEventMapper.insert(record);
        if (insertedRows != 1) {
            throw new BaseDomainException("Failed to persist telemetry event");
        }
        return record.toDomain();
    }
}
