package com.example.iotalarmcopilot.device.domain.model;

/**
 * 遥测值转换类型
 */
public enum TelemetryTransformType {
    // 原值本身为标准值
    IDENTITY,
    // 原值要乘系数
    SCALE,
    // 原值要减偏移
    OFFSET
}
