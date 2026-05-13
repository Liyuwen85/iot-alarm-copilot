package com.example.iotalarmcopilot.rule.application;

import com.example.iotalarmcopilot.contract.event.RuleTriggeredEvent;
import com.example.iotalarmcopilot.rule.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 遥测评估规则应用服务
 */
@Service
public class TelemetryRuleEvaluationApplicationService {

    private final RuleDefinitionRepository ruleDefinitionRepository;
    private final RuleExpressionEvaluator ruleExpressionEvaluator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TelemetryRuleEvaluationApplicationService(
            RuleDefinitionRepository ruleDefinitionRepository,
            RuleExpressionEvaluator ruleExpressionEvaluator,
            ApplicationEventPublisher applicationEventPublisher) {
        this.ruleDefinitionRepository = ruleDefinitionRepository;
        this.ruleExpressionEvaluator = ruleExpressionEvaluator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 评估遥测规则
     *
     * @param command
     * @return 触发结果列表
     */
    public List<RuleTriggeredResult> evaluate(EvaluateTelemetryRuleCommand command) {
        // 创建本上下文领域对象
        TelemetryRuleFacts facts = new TelemetryRuleFacts(
                command.telemetryEventId(),
                command.deviceId(),
                command.temperature(),
                command.humidity(),
                command.reportedAt());

        // 触发结果列表
        List<RuleTriggeredResult> triggeredResults = new ArrayList<>();
        // 加载规则定义列表
        for (RuleDefinition ruleDefinition : ruleDefinitionRepository.findEnabledTelemetryRules()) {
            // 是否满足规则触发条件
            RuleTriggeredResult result = ruleDefinition.evaluate(facts, ruleExpressionEvaluator);
            if (!result.triggered()) {
                continue;
            }
            // 发布规则触发事件
            applicationEventPublisher.publishEvent(new RuleTriggeredEvent(
                    result.ruleCode(),
                    result.telemetryEventId(),
                    result.deviceId(),
                    result.metricName(),
                    result.metricValue(),
                    result.threshold(),
                    result.triggeredAt()));
            triggeredResults.add(result);
        }
        return List.copyOf(triggeredResults);
    }
}
