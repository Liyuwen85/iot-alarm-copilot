package com.example.iotalarmcopilot.rule.domain;

/**
 * 规则表达式评估器（表达式执行器）
 */
public interface RuleExpressionEvaluator {

    /**
     * 评估（执行表达式）
     *
     * @param condition 规则条件
     * @param facts     遥测数据
     * @return
     */
    boolean evaluate(RuleCondition condition, TelemetryRuleFacts facts);
}
