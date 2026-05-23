package com.example.iotalarmcopilot.contract.device;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * 二元状态映射
 */
public record BinaryStateMappingContract(
        String activeLiteral,
        String inactiveLiteral,
        BigDecimal activeValue,
        BigDecimal inactiveValue) {

    public BinaryStateMappingContract {
        Objects.requireNonNull(activeLiteral, "activeLiteral must not be null");
        Objects.requireNonNull(inactiveLiteral, "inactiveLiteral must not be null");
        Objects.requireNonNull(activeValue, "activeValue must not be null");
        Objects.requireNonNull(inactiveValue, "inactiveValue must not be null");
        if (activeLiteral.isBlank()) {
            throw new IllegalArgumentException("activeLiteral must not be blank");
        }
        if (inactiveLiteral.isBlank()) {
            throw new IllegalArgumentException("inactiveLiteral must not be blank");
        }
        if (normalize(activeLiteral).equals(normalize(inactiveLiteral))) {
            throw new IllegalArgumentException("activeLiteral and inactiveLiteral must not be same");
        }
        if (activeValue.compareTo(inactiveValue) == 0) {
            throw new IllegalArgumentException("activeValue and inactiveValue must not be same");
        }
    }

    public BigDecimal map(String rawLiteral) {
        Objects.requireNonNull(rawLiteral, "rawLiteral must not be null");
        String normalized = normalize(rawLiteral);
        if (normalized.equals(normalize(activeLiteral))) {
            return activeValue;
        }
        if (normalized.equals(normalize(inactiveLiteral))) {
            return inactiveValue;
        }
        throw new IllegalArgumentException("Unsupported binary state literal: " + rawLiteral);
    }

    private static String normalize(String literal) {
        return literal.trim().toLowerCase(Locale.ROOT);
    }
}
