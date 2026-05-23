package com.example.iotalarmcopilot.contract.telemetry;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * 遥测基础指标名称
 *
 * @param value
 */
public record TelemetryMetricName(String value) {

    public static final TelemetryMetricName TEMPERATURE = new TelemetryMetricName("temperature");
    public static final TelemetryMetricName HUMIDITY = new TelemetryMetricName("humidity");

    public TelemetryMetricName {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("telemetry metricName must not be blank");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value;
    }
}
