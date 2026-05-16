package com.example.iotalarmcopilot.rule.application;

import com.example.iotalarmcopilot.contract.event.RuleTriggeredEvent;
import com.example.iotalarmcopilot.rule.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 遥测规则评估应用服务
 */
@Service
public class TelemetryRuleEvaluationApplicationService {

    private final RuleDefinitionRepository ruleDefinitionRepository;
    private final RuleExpressionEvaluator ruleExpressionEvaluator;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TelemetryRuleMatcher telemetryRuleMatcher = new TelemetryRuleMatcher();

    public TelemetryRuleEvaluationApplicationService(
            RuleDefinitionRepository ruleDefinitionRepository,
            RuleExpressionEvaluator ruleExpressionEvaluator,
            ApplicationEventPublisher applicationEventPublisher) {
        this.ruleDefinitionRepository = ruleDefinitionRepository;
        this.ruleExpressionEvaluator = ruleExpressionEvaluator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 遥测规则评估
     *
     * @param command
     * @return
     */
    public List<RuleTriggeredResult> evaluate(EvaluateTelemetryRuleCommand command) {
        TelemetryRuleFacts facts = TelemetryRuleFacts.fromTelemetryRecorded(
                command.telemetryEventId(),
                command.deviceId(),
                command.metrics(),
                command.reportedAt());

        List<RuleDefinition> telemetryRules = ruleDefinitionRepository.findTelemetryRules();
        List<RuleTriggeredResult> triggeredResults = telemetryRuleMatcher.evaluate(
                telemetryRules,
                facts,
                ruleExpressionEvaluator);
        for (RuleTriggeredResult result : triggeredResults) {
            applicationEventPublisher.publishEvent(new RuleTriggeredEvent(
                    result.ruleCode().value(),
                    result.telemetryEventId(),
                    result.deviceId().value(),
                    result.metricName(),
                    result.metricValue(),
                    result.threshold(),
                    result.triggeredAt()));
        }
        return triggeredResults;
    }
}
