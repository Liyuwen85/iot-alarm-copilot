package com.example.iotalarmcopilot.rule.domain;

import com.example.iotalarmcopilot.shared.BaseDomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * 遥测规则事实领域模型
 *
 * @param telemetryEventId
 * @param deviceId
 * @param temperature
 * @param humidity
 * @param reportedAt
 */
public record TelemetryRuleFacts(
        Long telemetryEventId,
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        Instant reportedAt) {

    // 支持的指标名
    private static final Set<String> SUPPORTED_METRIC_NAMES = Set.of("temperature", "humidity");

    public TelemetryRuleFacts {
        Objects.requireNonNull(telemetryEventId, "telemetryEventId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(reportedAt, "reportedAt must not be null");
        if (deviceId.isBlank()) {
            throw new BaseDomainException("deviceId must not be blank");
        }
    }

    /**
     * 转换为规则表达式变量
     *
     * @return
     */
    public Map<String, Object> toExpressionVariables() {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("telemetryEventId", telemetryEventId);
        variables.put("deviceId", deviceId);
        variables.put("temperature", temperature);
        variables.put("humidity", humidity);
        variables.put("reportedAt", reportedAt);
        variables.put("reportedAtEpochMs", reportedAt.toEpochMilli());
        return variables;
    }

    /**
     * 是否支持此指标
     *
     * @param metricName
     * @return
     */
    public static boolean supportsMetricName(String metricName) {
        return metricName != null
                && SUPPORTED_METRIC_NAMES.contains(metricName.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * 返回指标名对应的值
     *
     * @param metricName
     * @return
     */
    public BigDecimal metricValue(String metricName) {
        return switch (metricName.trim().toLowerCase(Locale.ROOT)) {
            case "temperature" -> temperature;
            case "humidity" -> humidity;
            default -> throw new BaseDomainException("Unsupported metricName: " + metricName);
        };
    }
}
