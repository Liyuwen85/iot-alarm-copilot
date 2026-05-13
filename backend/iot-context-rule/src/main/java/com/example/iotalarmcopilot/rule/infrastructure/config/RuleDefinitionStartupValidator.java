package com.example.iotalarmcopilot.rule.infrastructure.config;

import com.example.iotalarmcopilot.rule.domain.RuleDefinition;
import com.example.iotalarmcopilot.rule.domain.TelemetryRuleFacts;
import com.example.iotalarmcopilot.rule.infrastructure.expression.spel.SpelRuleExpressionEvaluator;
import com.example.iotalarmcopilot.rule.infrastructure.persistence.InMemoryRuleDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 启动后验证规则定义与表达式执行器是否正确
 */
@Component
public class RuleDefinitionStartupValidator implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(RuleDefinitionStartupValidator.class);

    private final InMemoryRuleDefinitionRepository ruleDefinitionRepository;
    private final SpelRuleExpressionEvaluator spelRuleExpressionEvaluator;

    public RuleDefinitionStartupValidator(
            InMemoryRuleDefinitionRepository ruleDefinitionRepository,
            SpelRuleExpressionEvaluator spelRuleExpressionEvaluator) {
        this.ruleDefinitionRepository = ruleDefinitionRepository;
        this.spelRuleExpressionEvaluator = spelRuleExpressionEvaluator;
    }

    /**
     * 所有单例Bean已初始化完成后执行
     */
    @Override
    public void afterSingletonsInstantiated() {
        TelemetryRuleFacts sampleFacts = new TelemetryRuleFacts(
                1L,
                "rule-validation-sample",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                Instant.now());
        List<RuleDefinition> rules = ruleDefinitionRepository.findEnabledTelemetryRules();
        for (RuleDefinition rule : rules) {
            spelRuleExpressionEvaluator.validate(rule.condition(), sampleFacts);
        }
        log.info("Validated {} enabled telemetry rules at startup", rules.size());
    }
}
