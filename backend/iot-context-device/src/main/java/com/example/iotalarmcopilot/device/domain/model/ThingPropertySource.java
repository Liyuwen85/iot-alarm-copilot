package com.example.iotalarmcopilot.device.domain.model;

/**
 * 属性来源
 */
public enum ThingPropertySource {
    // 遥测
    TELEMETRY,
    // 派生
    DERIVED,
    // 影子上报
    SHADOW_REPORTED,
    // 影子期望
    SHADOW_DESIRED
}
