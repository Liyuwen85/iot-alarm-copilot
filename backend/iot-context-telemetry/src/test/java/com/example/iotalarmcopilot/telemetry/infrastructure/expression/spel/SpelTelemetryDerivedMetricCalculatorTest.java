package com.example.iotalarmcopilot.telemetry.infrastructure.expression.spel;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.telemetry.domain.DerivedMetricDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpelTelemetryDerivedMetricCalculatorTest {

    private final SpelTelemetryDerivedMetricCalculator calculator = new SpelTelemetryDerivedMetricCalculator();

    @Test
    void should_compute_derived_metric_from_base_metrics() {
        TelemetryMetrics metrics = new TelemetryMetrics(Map.of(
                new TelemetryMetricName("temperature"), BigDecimal.valueOf(36.5),
                new TelemetryMetricName("humidity"), BigDecimal.valueOf(55)));
        DerivedMetricDefinition definition = new DerivedMetricDefinition(
                new TelemetryMetricName("heatIndex"),
                List.of(new TelemetryMetricName("temperature"), new TelemetryMetricName("humidity")),
                "temperature + (humidity * 0.1)",
                false,
                "C");

        TelemetryMetrics result = calculator.apply(metrics, List.of(definition));

        assertEquals("42.0", result.valueOf(new TelemetryMetricName("heatIndex")).toPlainString());
    }

    @Test
    void should_skip_optional_derived_metric_when_source_metric_is_missing() {
        Map<TelemetryMetricName, BigDecimal> values = new LinkedHashMap<>();
        values.put(new TelemetryMetricName("temperature"), BigDecimal.valueOf(36.5));
        TelemetryMetrics metrics = new TelemetryMetrics(values);
        DerivedMetricDefinition definition = new DerivedMetricDefinition(
                new TelemetryMetricName("heatIndex"),
                List.of(new TelemetryMetricName("temperature"), new TelemetryMetricName("humidity")),
                "temperature + (humidity * 0.1)",
                false,
                "C");

        TelemetryMetrics result = calculator.apply(metrics, List.of(definition));

        assertNull(result.valueOf(new TelemetryMetricName("heatIndex")));
    }

    @Test
    void should_reject_required_derived_metric_when_source_metric_is_missing() {
        Map<TelemetryMetricName, BigDecimal> values = new LinkedHashMap<>();
        values.put(new TelemetryMetricName("temperature"), BigDecimal.valueOf(36.5));
        TelemetryMetrics metrics = new TelemetryMetrics(values);
        DerivedMetricDefinition definition = new DerivedMetricDefinition(
                new TelemetryMetricName("heatIndex"),
                List.of(new TelemetryMetricName("temperature"), new TelemetryMetricName("humidity")),
                "temperature + (humidity * 0.1)",
                true,
                "C");

        assertThrows(BaseDomainException.class, () -> calculator.apply(metrics, List.of(definition)));
    }
}
