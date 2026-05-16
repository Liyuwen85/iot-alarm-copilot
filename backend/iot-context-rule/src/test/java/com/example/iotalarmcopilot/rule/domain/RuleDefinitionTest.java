package com.example.iotalarmcopilot.rule.domain;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.rule.infrastructure.expression.spel.SpelRuleExpressionEvaluator;
import com.example.iotalarmcopilot.BaseDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleDefinitionTest {

    private final SpelRuleExpressionEvaluator evaluator = new SpelRuleExpressionEvaluator();

    @Test
    void should_trigger_when_expression_matches() {
        RuleDefinition definition = RuleDefinition.create(
                new RuleCode("temperature_high"),
                "Temperature High",
                new TelemetryMetricName("temperature"),
                BigDecimal.valueOf(80),
                new RuleCondition(RuleExpressionLanguage.SPEL, "temperature >= 80"))
                .publish();
        TelemetryRuleFacts facts = TelemetryRuleFacts.fromTelemetryRecorded(
                101L,
                "dev-01",
                BigDecimal.valueOf(88),
                null,
                Instant.parse("2026-05-13T10:00:00Z"));

        Optional<RuleTriggeredResult> result = definition.evaluate(facts, evaluator);

        assertTrue(result.isPresent());
        assertEquals("temperature_high", result.get().ruleCode().value());
        assertEquals("dev-01", result.get().deviceId().value());
    }

    @Test
    void should_not_trigger_when_rule_disabled() {
        RuleDefinition definition = RuleDefinition.create(
                new RuleCode("temperature_high"),
                "Temperature High",
                new TelemetryMetricName("temperature"),
                BigDecimal.valueOf(80),
                new RuleCondition(RuleExpressionLanguage.SPEL, "temperature >= 80"))
                .publish()
                .disable();
        TelemetryRuleFacts facts = TelemetryRuleFacts.fromTelemetryRecorded(
                101L,
                "dev-01",
                BigDecimal.valueOf(88),
                null,
                Instant.parse("2026-05-13T10:00:00Z"));

        Optional<RuleTriggeredResult> result = definition.evaluate(facts, evaluator);

        assertTrue(result.isEmpty());
    }

    @Test
    void should_reject_disabling_draft_rule() {
        RuleDefinition draftRule = RuleDefinition.create(
                new RuleCode("temperature_high"),
                "Temperature High",
                new TelemetryMetricName("temperature"),
                BigDecimal.valueOf(80),
                new RuleCondition(RuleExpressionLanguage.SPEL, "temperature >= 80"));

        assertThrows(BaseDomainException.class, draftRule::disable);
    }
}
