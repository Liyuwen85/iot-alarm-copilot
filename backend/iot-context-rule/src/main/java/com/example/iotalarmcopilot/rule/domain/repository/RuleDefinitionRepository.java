package com.example.iotalarmcopilot.rule.domain.repository;

import com.example.iotalarmcopilot.rule.domain.model.RuleDefinition;

import java.util.List;

/**
 * 规则定义仓库
 */
public interface RuleDefinitionRepository {

    List<RuleDefinition> findTelemetryRules();
}
