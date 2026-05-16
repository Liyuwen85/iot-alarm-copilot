package com.example.iotalarmcopilot.rule.domain;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 规则表达式值对象
 *
 * @param language  支持的表达式语言
 * @param expression 表达式
 */
public record RuleCondition(
        RuleExpressionLanguage language,
        String expression) {

    public RuleCondition {
        Objects.requireNonNull(language, "language must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        if (expression.isBlank()) {
            throw new BaseDomainException("expression must not be blank");
        }
    }
}
