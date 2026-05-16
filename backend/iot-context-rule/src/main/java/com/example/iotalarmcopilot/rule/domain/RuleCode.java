package com.example.iotalarmcopilot.rule.domain;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 规则编码值对象
 *
 * @param value
 */
public record RuleCode(String value) {

    public RuleCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("rule code must not be blank");
        }
    }

    public String normalized() {
        return value.trim().toLowerCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
