package com.example.iotalarmcopilot.device.application;

import java.math.BigDecimal;

public record BinaryStateMappingVO(
        String activeLiteral,
        String inactiveLiteral,
        BigDecimal activeValue,
        BigDecimal inactiveValue) {
}
