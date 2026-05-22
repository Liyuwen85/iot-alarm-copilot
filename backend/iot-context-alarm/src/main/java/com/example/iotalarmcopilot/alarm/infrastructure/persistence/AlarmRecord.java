package com.example.iotalarmcopilot.alarm.infrastructure.persistence;

import com.example.iotalarmcopilot.alarm.domain.model.Alarm;
import com.example.iotalarmcopilot.alarm.domain.model.AlarmDedupKey;
import com.example.iotalarmcopilot.alarm.domain.model.AlarmSeverity;
import com.example.iotalarmcopilot.alarm.domain.model.AlarmStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 告警记录
 */
@Data
public class AlarmRecord {

    private Long id;
    private String dedupKey;
    private String ruleCode;
    private Long telemetryEventId;
    private String deviceId;
    private String metricName;
    private BigDecimal metricValue;
    private BigDecimal thresholdValue;
    private String severity;
    private String status;
    private Instant triggeredAt;
    private Instant acknowledgedAt;
    private Instant closedAt;

    public static AlarmRecord fromDomain(Alarm alarm) {
        AlarmRecord record = new AlarmRecord();
        record.setId(alarm.id());
        record.setDedupKey(alarm.dedupKey().value());
        record.setRuleCode(alarm.ruleCode());
        record.setTelemetryEventId(alarm.telemetryEventId());
        record.setDeviceId(alarm.deviceId());
        record.setMetricName(alarm.metricName());
        record.setMetricValue(alarm.metricValue());
        record.setThresholdValue(alarm.threshold());
        record.setSeverity(alarm.severity().name());
        record.setStatus(alarm.status().name());
        record.setTriggeredAt(alarm.triggeredAt());
        record.setAcknowledgedAt(alarm.acknowledgedAt());
        record.setClosedAt(alarm.closedAt());
        return record;
    }

    public Alarm toDomain() {
        return new Alarm(
                id,
                new AlarmDedupKey(dedupKey),
                ruleCode,
                telemetryEventId,
                deviceId,
                metricName,
                metricValue,
                thresholdValue,
                AlarmSeverity.valueOf(severity),
                AlarmStatus.valueOf(status),
                triggeredAt,
                acknowledgedAt,
                closedAt);
    }
}
