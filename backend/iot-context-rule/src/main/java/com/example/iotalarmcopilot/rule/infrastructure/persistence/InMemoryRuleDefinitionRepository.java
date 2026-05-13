package com.example.iotalarmcopilot.rule.infrastructure.persistence;

import com.example.iotalarmcopilot.rule.domain.*;
import com.example.iotalarmcopilot.rule.infrastructure.config.RuleProperties;
import com.example.iotalarmcopilot.shared.BaseDomainException;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于内存的规则定义存储
 */
@Repository
public class InMemoryRuleDefinitionRepository implements RuleDefinitionRepository {

    private final List<RuleDefinition> enabledTelemetryRules;

    public InMemoryRuleDefinitionRepository(RuleProperties ruleProperties) {
        this.enabledTelemetryRules = List.copyOf(loadDefinitions(ruleProperties));
    }

    @Override
    public List<RuleDefinition> findEnabledTelemetryRules() {
        return enabledTelemetryRules;
    }

    /**
     * 从配置加载规则定义
     * @param ruleProperties
     * @return
     */
    private List<RuleDefinition> loadDefinitions(RuleProperties ruleProperties) {
        if (!ruleProperties.isEnabled()) {
            return List.of();
        }
        List<RuleProperties.DefinitionItem> definitionItems = ruleProperties.getDefinitions() == null
                ? List.of()
                : ruleProperties.getDefinitions();
        List<RuleDefinition> definitions = definitionItems
                .stream()
                .map(this::toRuleDefinition)
                .toList();
        // 保证唯一
        validateUniqueCodes(definitions);
        // 保证监控指标能被支持
        validateMetricNames(definitions);
        return definitions.stream()
                .filter(RuleDefinition::enabled)
                .toList();
    }

    /**
     * 转换为规则定义领域对象
     * @param item
     * @return
     */
    private RuleDefinition toRuleDefinition(RuleProperties.DefinitionItem item) {
        if (item.getThreshold() == null) {
            throw new BaseDomainException("Rule threshold must not be null. code=" + item.getCode());
        }
        return new RuleDefinition(
                null,
                item.getCode(),
                item.getName(),
                item.isEnabled(),
                item.getMetricName(),
                item.getThreshold(),
                new RuleCondition(RuleExpressionLanguage.SPEL, item.getExpression()));
    }

    private void validateUniqueCodes(List<RuleDefinition> definitions) {
        Set<String> codes = new HashSet<>();
        for (RuleDefinition definition : definitions) {
            String normalizedCode = definition.code().trim().toLowerCase();
            if (!codes.add(normalizedCode)) {
                throw new BaseDomainException("Duplicate rule code found: " + definition.code());
            }
        }
    }

    private void validateMetricNames(List<RuleDefinition> definitions) {
        for (RuleDefinition definition : definitions) {
            if (!TelemetryRuleFacts.supportsMetricName(definition.metricName())) {
                throw new BaseDomainException("Unsupported metricName in rule: " + definition.metricName());
            }
        }
    }
}
