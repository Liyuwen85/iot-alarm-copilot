package com.example.iotalarmcopilot.rule.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 遥测规则批量匹配领域服务
 */
public class TelemetryRuleMatcher {

    public List<RuleTriggeredResult> evaluate(
            List<RuleDefinition> rules,
            TelemetryRuleFacts facts,
            RuleExpressionEvaluator ruleExpressionEvaluator) {
        Objects.requireNonNull(rules, "rules must not be null");
        Objects.requireNonNull(facts, "facts must not be null");
        Objects.requireNonNull(ruleExpressionEvaluator, "ruleExpressionEvaluator must not be null");

        List<RuleTriggeredResult> triggeredResults = new ArrayList<>();
        for (RuleDefinition rule : rules) {
            Optional<RuleTriggeredResult> triggeredResult = rule.evaluate(facts, ruleExpressionEvaluator);
            triggeredResult.ifPresent(triggeredResults::add);
        }
        return List.copyOf(triggeredResults);
    }
}
