package com.example.iotalarmcopilot.rule.infrastructure.config;

import com.example.iotalarmcopilot.rule.domain.model.RuleDefinition;
import com.example.iotalarmcopilot.rule.domain.model.TelemetryRuleFacts;
import com.example.iotalarmcopilot.rule.domain.repository.RuleDefinitionRepository;
import com.example.iotalarmcopilot.rule.infrastructure.expression.spel.SpelRuleExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 启动后校验规则定义与表达式执行器是否正确
 */
@Component
public class RuleDefinitionStartupValidator implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(RuleDefinitionStartupValidator.class);

    private final RuleDefinitionRepository ruleDefinitionRepository;
    private final SpelRuleExpressionEvaluator spelRuleExpressionEvaluator;

    public RuleDefinitionStartupValidator(
            RuleDefinitionRepository ruleDefinitionRepository,
            SpelRuleExpressionEvaluator spelRuleExpressionEvaluator) {
        this.ruleDefinitionRepository = ruleDefinitionRepository;
        this.spelRuleExpressionEvaluator = spelRuleExpressionEvaluator;
    }

    @Override
    public void afterSingletonsInstantiated() {
        TelemetryRuleFacts sampleFacts = TelemetryRuleFacts.fromTelemetryRecorded(
                1L,
                "rule-validation-sample",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                Instant.now());
        List<RuleDefinition> rules = ruleDefinitionRepository.findTelemetryRules();
        for (RuleDefinition rule : rules) {
            spelRuleExpressionEvaluator.validate(rule.condition(), sampleFacts);
        }
        log.info("Validated {} telemetry rules at startup", rules.size());
    }
}
