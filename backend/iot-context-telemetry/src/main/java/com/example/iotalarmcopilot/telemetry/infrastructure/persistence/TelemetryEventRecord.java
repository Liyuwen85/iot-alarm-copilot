package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 遥测数据持久化实体
 */
@Data
public class TelemetryEventRecord {

    private Long id;
    private String deviceId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private Instant reportedAt;
    private String rawJson;

    public static TelemetryEventRecord fromDomain(TelemetryEvent event) {
        TelemetryEventRecord record = new TelemetryEventRecord();
        record.setId(event.id());
        record.setDeviceId(event.deviceId());
        record.setTemperature(event.temperature());
        record.setHumidity(event.humidity());
        record.setReportedAt(event.reportedAt());
        record.setRawJson(event.rawJson());
        return record;
    }

    public TelemetryEvent toDomain() {
        return new TelemetryEvent(id, deviceId, temperature, humidity, reportedAt, rawJson);
    }

}
