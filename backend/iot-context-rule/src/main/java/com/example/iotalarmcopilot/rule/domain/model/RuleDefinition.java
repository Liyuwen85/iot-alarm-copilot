package com.example.iotalarmcopilot.rule.domain.model;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.rule.domain.policy.RuleStatusPolicy;
import com.example.iotalarmcopilot.rule.domain.service.RuleExpressionEvaluator;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * 规则定义领域实体
 *
 * @param id
 * @param code
 * @param name
 * @param status
 * @param metricName
 * @param threshold
 * @param condition
 */
public record RuleDefinition(
        Long id,
        RuleCode code,
        String name,
        RuleStatus status,
        TelemetryMetricName metricName,
        BigDecimal threshold,
        RuleCondition condition) {

    public static RuleDefinition create(
            RuleCode code,
            String name,
            TelemetryMetricName metricName,
            BigDecimal threshold,
            RuleCondition condition) {
        return new RuleDefinition(
                null,
                code,
                name,
                RuleStatus.DRAFT,
                metricName,
                threshold,
                condition);
    }

    public RuleDefinition {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(metricName, "metricName must not be null");
        Objects.requireNonNull(threshold, "threshold must not be null");
        Objects.requireNonNull(condition, "condition must not be null");
        if (name.isBlank()) {
            throw new BaseDomainException("name must not be blank");
        }
    }

    public RuleDefinition publish() {
        RuleStatusPolicy.ensurePublishAllowed(status);
        return new RuleDefinition(id, code, name, RuleStatus.ACTIVE, metricName, threshold, condition);
    }

    public RuleDefinition disable() {
        RuleStatusPolicy.ensureDisableAllowed(status);
        return new RuleDefinition(id, code, name, RuleStatus.INACTIVE, metricName, threshold, condition);
    }

    public RuleDefinition reactivate() {
        RuleStatusPolicy.ensureReactivateAllowed(status);
        return new RuleDefinition(id, code, name, RuleStatus.ACTIVE, metricName, threshold, condition);
    }

    public boolean executable() {
        return status.executable();
    }

    public Optional<RuleTriggeredResult> evaluate(
            TelemetryRuleFacts facts,
            RuleExpressionEvaluator ruleExpressionEvaluator) {
        if (!executable()) {
            return Optional.empty();
        }
        boolean matched = ruleExpressionEvaluator.evaluate(condition, facts);
        if (!matched) {
            return Optional.empty();
        }
        return Optional.of(new RuleTriggeredResult(
                code,
                facts.telemetryEventId(),
                facts.deviceId(),
                metricName,
                facts.metricValue(metricName),
                threshold,
                facts.reportedAt()));
    }
}
