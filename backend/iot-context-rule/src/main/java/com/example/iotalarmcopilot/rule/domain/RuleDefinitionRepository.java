package com.example.iotalarmcopilot.rule.domain;

import java.util.List;

/**
 * 规则定义仓库
 */
public interface RuleDefinitionRepository {

    List<RuleDefinition> findEnabledTelemetryRules();
}
