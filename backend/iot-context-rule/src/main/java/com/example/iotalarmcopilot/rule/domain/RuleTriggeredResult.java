package com.example.iotalarmcopilot.rule.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 规则触发结果领域模型
 *
 * @param triggered
 * @param ruleCode
 * @param telemetryEventId
 * @param deviceId
 * @param metricName
 * @param metricValue
 * @param threshold
 * @param triggeredAt
 */
public record RuleTriggeredResult(
        boolean triggered,
        String ruleCode,
        Long telemetryEventId,
        String deviceId,
        String metricName,
        BigDecimal metricValue,
        BigDecimal threshold,
        Instant triggeredAt) {

    /**
     * 不满足规则条件
     *
     * @return
     */
    public static RuleTriggeredResult notTriggered() {
        return new RuleTriggeredResult(false, null, null, null, null, null, null, null);
    }

    /**
     * 满足触发条件
     *
     * @param ruleCode
     * @param telemetryEventId
     * @param deviceId
     * @param metricName
     * @param metricValue
     * @param threshold
     * @param triggeredAt
     * @return
     */
    public static RuleTriggeredResult triggered(
            String ruleCode,
            Long telemetryEventId,
            String deviceId,
            String metricName,
            BigDecimal metricValue,
            BigDecimal threshold,
            Instant triggeredAt) {
        return new RuleTriggeredResult(
                true,
                ruleCode,
                telemetryEventId,
                deviceId,
                metricName,
                metricValue,
                threshold,
                triggeredAt);
    }
}
