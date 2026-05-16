package com.example.iotalarmcopilot.rule.domain;

/**
 * 规则状态值对象
 */
public enum RuleStatus {
    DRAFT,
    ACTIVE,
    INACTIVE;

    public boolean executable() {
        return this == ACTIVE;
    }
}
