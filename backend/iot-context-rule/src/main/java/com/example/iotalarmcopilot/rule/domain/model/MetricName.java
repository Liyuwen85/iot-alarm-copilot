package com.example.iotalarmcopilot.rule.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * 指标名值对象
 *
 * @param value
 */
public record MetricName(String value) {

    public MetricName {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("metricName must not be blank");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value;
    }
}
