package com.example.iotalarmcopilot.rule.application;

import com.example.iotalarmcopilot.contract.event.RuleTriggeredEvent;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.rule.domain.RuleCode;
import com.example.iotalarmcopilot.rule.domain.RuleCondition;
import com.example.iotalarmcopilot.rule.domain.RuleDefinition;
import com.example.iotalarmcopilot.rule.domain.RuleDefinitionRepository;
import com.example.iotalarmcopilot.rule.domain.RuleExpressionEvaluator;
import com.example.iotalarmcopilot.rule.domain.RuleExpressionLanguage;
import com.example.iotalarmcopilot.rule.domain.TelemetryRuleFacts;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelemetryRuleEvaluationApplicationServiceTest {

    @Test
    void should_publish_rule_triggered_event_when_rule_matches() {
        RuleDefinitionRepository repository = mock(RuleDefinitionRepository.class);
        RuleExpressionEvaluator evaluator = mock(RuleExpressionEvaluator.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TelemetryRuleEvaluationApplicationService service =
                new TelemetryRuleEvaluationApplicationService(repository, evaluator, publisher);
        RuleDefinition rule = RuleDefinition.create(
                new RuleCode("temperature_high"),
                "Temperature High",
                new TelemetryMetricName("temperature"),
                BigDecimal.valueOf(80),
                new RuleCondition(RuleExpressionLanguage.SPEL, "temperature >= 80"))
                .publish();
        EvaluateTelemetryRuleCommand command = new EvaluateTelemetryRuleCommand(
                101L,
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(88), null),
                Instant.parse("2026-05-13T10:00:00Z"));

        when(repository.findTelemetryRules()).thenReturn(List.of(rule));
        when(evaluator.evaluate(any(RuleCondition.class), any(TelemetryRuleFacts.class))).thenReturn(true);

        var results = service.evaluate(command);

        assertEquals(1, results.size());
        verify(publisher).publishEvent(new RuleTriggeredEvent(
                "temperature_high",
                101L,
                "dev-01",
                new TelemetryMetricName("temperature"),
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                Instant.parse("2026-05-13T10:00:00Z")));
    }

    @Test
    void should_not_publish_event_when_rule_does_not_match() {
        RuleDefinitionRepository repository = mock(RuleDefinitionRepository.class);
        RuleExpressionEvaluator evaluator = mock(RuleExpressionEvaluator.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TelemetryRuleEvaluationApplicationService service =
                new TelemetryRuleEvaluationApplicationService(repository, evaluator, publisher);
        RuleDefinition rule = RuleDefinition.create(
                new RuleCode("temperature_high"),
                "Temperature High",
                new TelemetryMetricName("temperature"),
                BigDecimal.valueOf(80),
                new RuleCondition(RuleExpressionLanguage.SPEL, "temperature >= 80"))
                .publish();
        EvaluateTelemetryRuleCommand command = new EvaluateTelemetryRuleCommand(
                101L,
                "dev-01",
                TelemetryMetrics.ofTemperatureAndHumidity(BigDecimal.valueOf(75), null),
                Instant.parse("2026-05-13T10:00:00Z"));

        when(repository.findTelemetryRules()).thenReturn(List.of(rule));
        when(evaluator.evaluate(any(RuleCondition.class), any(TelemetryRuleFacts.class))).thenReturn(false);

        var results = service.evaluate(command);

        assertEquals(0, results.size());
        verify(publisher, never()).publishEvent(any(RuleTriggeredEvent.class));
    }
}
