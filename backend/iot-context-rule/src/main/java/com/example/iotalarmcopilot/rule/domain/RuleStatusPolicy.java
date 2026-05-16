package com.example.iotalarmcopilot.rule.domain;

import com.example.iotalarmcopilot.BaseDomainException;

/**
 * 规则状态策略
 */
public final class RuleStatusPolicy {

    private RuleStatusPolicy() {
    }

    /**
     * 确保可以发布规则（草稿状态）
     *
     * @param status
     */
    public static void ensurePublishAllowed(RuleStatus status) {
        if (status != RuleStatus.DRAFT) {
            throw new BaseDomainException("Only draft rule can be published");
        }
    }

    /**
     * 确保可以禁用规则（活跃状态）
     *
     * @param status
     */
    public static void ensureDisableAllowed(RuleStatus status) {
        if (status != RuleStatus.ACTIVE) {
            throw new BaseDomainException("Only active rule can be disabled");
        }
    }

    /**
     * 确保可以启用规则（禁用状态）
     *
     * @param status
     */
    public static void ensureReactivateAllowed(RuleStatus status) {
        if (status != RuleStatus.INACTIVE) {
            throw new BaseDomainException("Only inactive rule can be reactivated");
        }
    }
}
