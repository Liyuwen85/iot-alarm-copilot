package com.example.iotalarmcopilot.telemetry.infrastructure.timeseries;

import com.example.iotalarmcopilot.telemetry.application.TelemetryEventVO;
import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;
import com.example.iotalarmcopilot.telemetry.infrastructure.codec.TelemetryMetricsJsonCodec;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 遥测点记录TSDB
 */
@Data
public class TelemetryPointRecord {

    private Long telemetryEventId;
    private String deviceId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String metricsJson;
    private Instant reportedAt;
    private String rawJson;

    static TelemetryPointRecord fromDomain(TelemetryEvent event) {
        TelemetryPointRecord record = new TelemetryPointRecord();
        record.setTelemetryEventId(event.id());
        record.setDeviceId(event.deviceId().value());
        record.setTemperature(event.temperature());
        record.setHumidity(event.humidity());
        record.setMetricsJson(TelemetryMetricsJsonCodec.encode(event.metrics()));
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
                metricsJson,
                reportedAt,
                rawJson);
    }
}
