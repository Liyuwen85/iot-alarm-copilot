package com.example.iotalarmcopilot.rule.domain;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.BaseDomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 遥测事件-领域计算输入模型
 *
 * @param telemetryEventId
 * @param deviceId
 * @param metrics
 * @param reportedAt
 */
public record TelemetryRuleFacts(
        Long telemetryEventId,
        DeviceId deviceId,
        TelemetryMetrics metrics,
        Instant reportedAt) {

    private static final Set<TelemetryMetricName> SUPPORTED_METRIC_NAMES = Set.of(
            TelemetryMetricName.TEMPERATURE,
            TelemetryMetricName.HUMIDITY);

    public static TelemetryRuleFacts fromTelemetryRecorded(
            Long telemetryEventId,
            String deviceId,
            TelemetryMetrics metrics,
            Instant reportedAt) {
        return new TelemetryRuleFacts(
                telemetryEventId,
                new DeviceId(deviceId),
                metrics,
                reportedAt);
    }

    public static TelemetryRuleFacts fromTelemetryRecorded(
            Long telemetryEventId,
            String deviceId,
            BigDecimal temperature,
            BigDecimal humidity,
            Instant reportedAt) {
        return fromTelemetryRecorded(
                telemetryEventId,
                deviceId,
                TelemetryMetrics.ofTemperatureAndHumidity(temperature, humidity),
                reportedAt);
    }

    public TelemetryRuleFacts {
        Objects.requireNonNull(telemetryEventId, "telemetryEventId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(metrics, "metrics must not be null");
        Objects.requireNonNull(reportedAt, "reportedAt must not be null");
    }

    public Map<String, Object> toExpressionVariables() {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("telemetryEventId", telemetryEventId);
        variables.put("deviceId", deviceId.value());
        variables.putAll(metrics.toFlatMap());
        variables.put("reportedAt", reportedAt);
        variables.put("reportedAtEpochMs", reportedAt.toEpochMilli());
        return variables;
    }

    public static boolean supportsMetricName(TelemetryMetricName metricName) {
        return metricName != null && SUPPORTED_METRIC_NAMES.contains(metricName);
    }

    public BigDecimal metricValue(TelemetryMetricName metricName) {
        BigDecimal value = metrics.valueOf(metricName);
        if (value == null) {
            throw new BaseDomainException("Metric value not found: " + metricName.value());
        }
        return value;
    }
}
