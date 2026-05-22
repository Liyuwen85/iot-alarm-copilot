package com.example.iotalarmcopilot.rule.infrastructure.persistence;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetricName;
import com.example.iotalarmcopilot.rule.domain.model.RuleCode;
import com.example.iotalarmcopilot.rule.domain.model.RuleCondition;
import com.example.iotalarmcopilot.rule.domain.model.RuleDefinition;
import com.example.iotalarmcopilot.rule.domain.model.RuleExpressionLanguage;
import com.example.iotalarmcopilot.rule.domain.model.TelemetryRuleFacts;
import com.example.iotalarmcopilot.rule.domain.repository.RuleDefinitionRepository;
import com.example.iotalarmcopilot.rule.infrastructure.config.RuleProperties;
import com.example.iotalarmcopilot.BaseDomainException;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于内存的规则定义存储
 */
@Repository
public class InMemoryRuleDefinitionRepository implements RuleDefinitionRepository {

    private final List<RuleDefinition> telemetryRules;

    public InMemoryRuleDefinitionRepository(RuleProperties ruleProperties) {
        this.telemetryRules = List.copyOf(loadDefinitions(ruleProperties));
    }

    @Override
    public List<RuleDefinition> findTelemetryRules() {
        return telemetryRules;
    }

    private List<RuleDefinition> loadDefinitions(RuleProperties ruleProperties) {
        if (!ruleProperties.isEnabled()) {
            return List.of();
        }
        List<RuleProperties.DefinitionItem> definitionItems = ruleProperties.getDefinitions() == null
                ? List.of()
                : ruleProperties.getDefinitions();
        List<RuleDefinition> definitions = definitionItems.stream()
                .map(this::toRuleDefinition)
                .toList();
        validateUniqueCodes(definitions);
        validateMetricNames(definitions);
        return definitions;
    }

    private RuleDefinition toRuleDefinition(RuleProperties.DefinitionItem item) {
        if (item.getThreshold() == null) {
            throw new BaseDomainException("Rule threshold must not be null. code=" + item.getCode());
        }
        RuleDefinition definition = RuleDefinition.create(
                new RuleCode(item.getCode()),
                item.getName(),
                new TelemetryMetricName(item.getMetricName()),
                item.getThreshold(),
                new RuleCondition(RuleExpressionLanguage.SPEL, item.getExpression()));
        return item.isEnabled() ? definition.publish() : definition.publish().disable();
    }

    private void validateUniqueCodes(List<RuleDefinition> definitions) {
        Set<String> codes = new HashSet<>();
        for (RuleDefinition definition : definitions) {
            String normalizedCode = definition.code().normalized();
            if (!codes.add(normalizedCode)) {
                throw new BaseDomainException("Duplicate rule code found: " + definition.code().value());
            }
        }
    }

    private void validateMetricNames(List<RuleDefinition> definitions) {
        for (RuleDefinition definition : definitions) {
            if (!TelemetryRuleFacts.supportsMetricName(definition.metricName())) {
                throw new BaseDomainException("Unsupported metricName in rule: " + definition.metricName().value());
            }
        }
    }
}
