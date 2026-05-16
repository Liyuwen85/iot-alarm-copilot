package com.example.iotalarmcopilot.telemetry.infrastructure.timeseries;

import com.example.iotalarmcopilot.telemetry.application.TelemetryEventVO;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * TimescaleDB 中的遥测热点点位
 */
@Data
public class TelemetryPointRecord {

    private Long telemetryEventId;
    private String deviceId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private Instant reportedAt;
    private String rawJson;

    static TelemetryPointRecord fromDomain(TelemetryEvent event) {
        TelemetryPointRecord record = new TelemetryPointRecord();
        record.setTelemetryEventId(event.id());
        record.setDeviceId(event.deviceId().value());
        record.setTemperature(event.temperature());
        record.setHumidity(event.humidity());
        record.setReportedAt(event.reportedAt());
        record.setRawJson(event.rawJson());
        return record;
    }

    TelemetryEventVO toVO() {
        return new TelemetryEventVO(
                telemetryEventId,
                deviceId,
                temperature,
                humidity,
                reportedAt,
                rawJson);
    }
}
