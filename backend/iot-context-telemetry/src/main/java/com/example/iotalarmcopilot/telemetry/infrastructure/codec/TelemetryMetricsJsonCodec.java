package com.example.iotalarmcopilot.telemetry.infrastructure.codec;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * metric序列化/反序列化
 */
public final class TelemetryMetricsJsonCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, BigDecimal>> METRICS_TYPE = new TypeReference<>() {
    };

    private TelemetryMetricsJsonCodec() {
    }

    public static String encode(TelemetryMetrics metrics) {
        try {
            // 扁平化
            return OBJECT_MAPPER.writeValueAsString(metrics.toFlatMap());
        } catch (JsonProcessingException exception) {
            throw new BaseDomainException("Failed to encode telemetry metrics json");
        }
    }

    public static TelemetryMetrics decode(String json) {
        if (json == null || json.isBlank()) {
            throw new BaseDomainException("telemetry metrics json must not be blank");
        }
        try {
            Map<String, BigDecimal> flatMap = OBJECT_MAPPER.readValue(json, METRICS_TYPE);
            Map<TelemetryMetricName, BigDecimal> metrics = new LinkedHashMap<>();
            for (Map.Entry<String, BigDecimal> entry : flatMap.entrySet()) {
                metrics.put(new TelemetryMetricName(entry.getKey()), entry.getValue());
            }
            return new TelemetryMetrics(metrics);
        } catch (Exception exception) {
            throw new BaseDomainException("Failed to decode telemetry metrics json");
        }
    }
}
