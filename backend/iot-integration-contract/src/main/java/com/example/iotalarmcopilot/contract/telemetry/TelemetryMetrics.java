package com.example.iotalarmcopilot.contract.telemetry;

import com.example.iotalarmcopilot.BaseDomainException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 遥测数据指标
 *
 * @param values
 */
public record TelemetryMetrics(Map<TelemetryMetricName, BigDecimal> values) {

    public static TelemetryMetrics ofTemperatureAndHumidity(
            BigDecimal temperature,
            BigDecimal humidity) {
        Map<TelemetryMetricName, BigDecimal> values = new LinkedHashMap<>();
        if (temperature != null) {
            values.put(TelemetryMetricName.TEMPERATURE, temperature);
        }
        if (humidity != null) {
            values.put(TelemetryMetricName.HUMIDITY, humidity);
        }
        return new TelemetryMetrics(values);
    }

    public TelemetryMetrics {
        Objects.requireNonNull(values, "values must not be null");
        if (values.isEmpty()) {
            throw new BaseDomainException("Telemetry metrics must contain at least one metric");
        }
        values = Map.copyOf(values);
    }

    public BigDecimal valueOf(TelemetryMetricName metricName) {
        return values.get(metricName);
    }

    public BigDecimal temperature() {
        return valueOf(TelemetryMetricName.TEMPERATURE);
    }

    public BigDecimal humidity() {
        return valueOf(TelemetryMetricName.HUMIDITY);
    }

    /**
     * 扁平结构
     *
     * @return
     */
    public Map<String, BigDecimal> toFlatMap() {
        Map<String, BigDecimal> flatMap = new LinkedHashMap<>();
        for (Map.Entry<TelemetryMetricName, BigDecimal> entry : values.entrySet()) {
            flatMap.put(entry.getKey().value(), entry.getValue());
        }
        return Map.copyOf(flatMap);
    }
}
