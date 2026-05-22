package com.example.iotalarmcopilot.telemetry.infrastructure.expression.spel;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.telemetry.application.TelemetryDerivedMetricCalculator;
import com.example.iotalarmcopilot.telemetry.domain.DerivedMetricDefinition;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Spel遥测派生指标计算器
 */
@Component
public class SpelTelemetryDerivedMetricCalculator implements TelemetryDerivedMetricCalculator {

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ConcurrentMap<String, Expression> expressionCache = new ConcurrentHashMap<>();
    private final SimpleEvaluationContext evaluationContext = SimpleEvaluationContext
            .forPropertyAccessors(new MapAccessor())
            .build();

    /**
     * 计算遥测派生指标
     *
     * @param baseMetrics
     * @param derivedMetricDefinitions
     * @return
     */
    @Override
    public TelemetryMetrics apply(TelemetryMetrics baseMetrics, List<DerivedMetricDefinition> derivedMetricDefinitions) {
        if (derivedMetricDefinitions == null || derivedMetricDefinitions.isEmpty()) {
            return baseMetrics;
        }
        // 原始指标值
        Map<TelemetryMetricName, BigDecimal> values = new LinkedHashMap<>(baseMetrics.values());
        for (DerivedMetricDefinition definition : derivedMetricDefinitions) {
            BigDecimal value = evaluate(definition, values);
            if (value != null) {
                values.put(definition.metricName(), value);
            } else if (definition.required()) {
                throw new BaseDomainException("Required derived metric is missing. metric="
                        + definition.metricName().value());
            }
        }
        return new TelemetryMetrics(values);
    }

    private BigDecimal evaluate(
            DerivedMetricDefinition definition,
            Map<TelemetryMetricName, BigDecimal> values) {
        // 未匹配到
        if (definition.sourceMetrics().stream().anyMatch(sourceMetric -> values.get(sourceMetric) == null)) {
            if (definition.required()) {
                throw new BaseDomainException("Required derived metric source is missing. metric="
                        + definition.metricName().value());
            }
            return null;
        }
        Map<String, Object> variables = new LinkedHashMap<>();
        for (TelemetryMetricName sourceMetric : definition.sourceMetrics()) {
            variables.put(sourceMetric.value(), values.get(sourceMetric));
        }
        try {
            Object result = compile(definition.expression()).getValue(evaluationContext, variables);
            if (result == null) {
                return null;
            }
            if (result instanceof BigDecimal decimal) {
                return decimal;
            }
            if (result instanceof Number number) {
                return new BigDecimal(number.toString());
            }
            return new BigDecimal(result.toString());
        } catch (Exception exception) {
            throw new BaseDomainException("Failed to evaluate derived metric. metric="
                    + definition.metricName().value());
        }
    }

    private Expression compile(String expression) {
        return expressionCache.computeIfAbsent(expression, expressionParser::parseExpression);
    }
}
