package com.example.iotalarmcopilot.mockdevice.domain;

import java.math.BigDecimal;

/**
 * 网关上报实体
 */
public record GatewayUplinkMessage(
        String deviceId,
        String gatewayId,
        String source,
        BigDecimal temperature,
        BigDecimal humidity,
        String ts) {
}
