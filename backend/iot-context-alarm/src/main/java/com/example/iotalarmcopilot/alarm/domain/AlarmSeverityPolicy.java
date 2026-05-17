package com.example.iotalarmcopilot.alarm.domain;

import java.util.Locale;

/**
 * 告警级别策略
 */
public final class AlarmSeverityPolicy {

    private AlarmSeverityPolicy() {
    }

    /**
     * 解析告警级别（领域判定策略）
     *
     * @param ruleCode
     * @return
     */
    public static AlarmSeverity resolve(String ruleCode) {
        String normalized = ruleCode == null ? "" : ruleCode.toLowerCase(Locale.ROOT);
        if (normalized.contains("critical")) {
            return AlarmSeverity.CRITICAL;
        }
        return AlarmSeverity.WARN;
    }
}
