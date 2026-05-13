package com.example.iotalarmcopilot.rule.infrastructure.expression.spel;

import com.example.iotalarmcopilot.rule.domain.RuleCondition;
import com.example.iotalarmcopilot.rule.domain.RuleExpressionEvaluator;
import com.example.iotalarmcopilot.rule.domain.RuleExpressionLanguage;
import com.example.iotalarmcopilot.rule.domain.TelemetryRuleFacts;
import com.example.iotalarmcopilot.shared.BaseDomainException;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * SpEL规则表达式评估器实现
 */
@Component
public class SpelRuleExpressionEvaluator implements RuleExpressionEvaluator {

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ConcurrentMap<String, Expression> expressionCache = new ConcurrentHashMap<>();
    private final SimpleEvaluationContext evaluationContext =
            SimpleEvaluationContext.forPropertyAccessors(new MapAccessor()).build();

    @Override
    public boolean evaluate(RuleCondition condition, TelemetryRuleFacts facts) {
        if (condition.language() != RuleExpressionLanguage.SPEL) {
            throw new BaseDomainException("Unsupported rule language: " + condition.language());
        }
        try {
            Expression expression = compile(condition.expression());
            Boolean matched = evaluateAsBoolean(expression, facts.toExpressionVariables());
            return Boolean.TRUE.equals(matched);
        } catch (Exception exception) {
            throw new BaseDomainException("Failed to evaluate SpEL rule: " + exception.getMessage());
        }
    }

    /**
     * 验证规则表达式是否有效
     *
     * @param condition
     * @param sampleFacts
     */
    public void validate(RuleCondition condition, TelemetryRuleFacts sampleFacts) {
        if (condition.language() != RuleExpressionLanguage.SPEL) {
            throw new BaseDomainException("Unsupported rule language: " + condition.language());
        }
        try {
            Expression expression = compile(condition.expression());
            evaluateAsBoolean(expression, sampleFacts.toExpressionVariables());
        } catch (Exception exception) {
            throw new BaseDomainException("Invalid SpEL rule expression: " + exception.getMessage());
        }
    }

    private Expression compile(String expression) {
        return expressionCache.computeIfAbsent(expression, expressionParser::parseExpression);
    }

    private Boolean evaluateAsBoolean(Expression expression, Map<String, Object> variables) {
        return expression.getValue(evaluationContext, variables, Boolean.class);
    }
}
