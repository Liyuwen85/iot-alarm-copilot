package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * 二值状态类指标映射
 *
 * @param activeLiteral   原始可用字面量
 * @param inactiveLiteral 原始不不可用字面量
 * @param activeValue     映射后的可用值
 * @param inactiveValue   映射后的不可用值
 */
public record BinaryStateMapping(
        String activeLiteral,
        String inactiveLiteral,
        BigDecimal activeValue,
        BigDecimal inactiveValue) {

    public BinaryStateMapping {
        Objects.requireNonNull(activeLiteral, "activeLiteral must not be null");
        Objects.requireNonNull(inactiveLiteral, "inactiveLiteral must not be null");
        Objects.requireNonNull(activeValue, "activeValue must not be null");
        Objects.requireNonNull(inactiveValue, "inactiveValue must not be null");
        if (activeLiteral.isBlank()) {
            throw new BaseDomainException("activeLiteral must not be blank");
        }
        if (inactiveLiteral.isBlank()) {
            throw new BaseDomainException("inactiveLiteral must not be blank");
        }
        if (normalize(activeLiteral).equals(normalize(inactiveLiteral))) {
            throw new BaseDomainException("activeLiteral and inactiveLiteral must not be same");
        }
        if (activeValue.compareTo(inactiveValue) == 0) {
            throw new BaseDomainException("activeValue and inactiveValue must not be same");
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
        throw new BaseDomainException("Unsupported binary state literal: " + rawLiteral);
    }

    private static String normalize(String literal) {
        return literal.trim().toLowerCase(Locale.ROOT);
    }
}
