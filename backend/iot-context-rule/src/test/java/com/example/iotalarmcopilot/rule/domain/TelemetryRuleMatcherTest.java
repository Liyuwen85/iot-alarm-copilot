package com.example.iotalarmcopilot.rule.domain;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelemetryRuleMatcherTest {

    private final TelemetryRuleMatcher matcher = new TelemetryRuleMatcher();
    private final RuleExpressionEvaluator evaluator = (condition, facts) -> true;

    @Test
    void should_only_return_triggered_results_for_executable_rules() {
        RuleDefinition activeRule = RuleDefinition.create(
                new RuleCode("temperature_high"),
                "Temperature High",
                new TelemetryMetricName("temperature"),
                BigDecimal.valueOf(80),
                new RuleCondition(RuleExpressionLanguage.SPEL, "temperature >= 80"))
                .publish();
        RuleDefinition inactiveRule = RuleDefinition.create(
                new RuleCode("humidity_high"),
                "Humidity High",
                new TelemetryMetricName("humidity"),
                BigDecimal.valueOf(70),
                new RuleCondition(RuleExpressionLanguage.SPEL, "humidity >= 70"))
                .publish()
                .disable();
        TelemetryRuleFacts facts = TelemetryRuleFacts.fromTelemetryRecorded(
                101L,
                "dev-01",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(72),
                Instant.parse("2026-05-13T10:00:00Z"));

        List<RuleTriggeredResult> results = matcher.evaluate(
                List.of(activeRule, inactiveRule),
                facts,
                evaluator);

        assertEquals(1, results.size());
        assertEquals("temperature_high", results.getFirst().ruleCode().value());
    }
}
