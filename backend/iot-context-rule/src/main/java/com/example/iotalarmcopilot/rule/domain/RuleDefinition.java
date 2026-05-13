package com.example.iotalarmcopilot.rule.domain;

import com.example.iotalarmcopilot.shared.BaseDomainException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 规则定义领域实体
 *
 * @param id
 * @param code
 * @param name
 * @param enabled
 * @param metricName
 * @param threshold
 * @param condition
 */
public record RuleDefinition(
        Long id,
        String code,
        String name,
        boolean enabled,
        String metricName,
        BigDecimal threshold,
        RuleCondition condition) {

    public RuleDefinition {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(metricName, "metricName must not be null");
        Objects.requireNonNull(threshold, "threshold must not be null");
        Objects.requireNonNull(condition, "condition must not be null");
        if (code.isBlank()) {
            throw new BaseDomainException("code must not be blank");
        }
        if (name.isBlank()) {
            throw new BaseDomainException("name must not be blank");
        }
        if (metricName.isBlank()) {
            throw new BaseDomainException("metricName must not be blank");
        }
    }

    /**
     * 评估规则
     *
     * @param facts                   遥测数据
     * @param ruleExpressionEvaluator 表达式执行器
     * @return
     */
    public RuleTriggeredResult evaluate(TelemetryRuleFacts facts, RuleExpressionEvaluator ruleExpressionEvaluator) {
        if (!enabled) {
            return RuleTriggeredResult.notTriggered();
        }
        boolean matched = ruleExpressionEvaluator.evaluate(condition, facts);
        if (!matched) {
            return RuleTriggeredResult.notTriggered();
        }
        // 匹配就触发规则事件
        return RuleTriggeredResult.triggered(
                code,
                facts.telemetryEventId(),
                facts.deviceId(),
                metricName,
                facts.metricValue(metricName),
                threshold,
                facts.reportedAt());
    }
}
