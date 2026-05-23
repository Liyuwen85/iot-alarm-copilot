package com.example.iotalarmcopilot.inspection.domain.policy;

import java.util.Locale;

/**
 * 巡检建议策略
 */
public final class InspectionAdvicePolicy {

    private InspectionAdvicePolicy() {
    }

    public static String buildSummary(String ruleCode, String deviceId) {
        return "设备 " + deviceId + " 规则 " + ruleCode + " 的巡检工单";
    }

    public static String buildSuggestion(String severity, String ruleCode) {
        String normalizedSeverity = severity == null ? "" : severity.toUpperCase(Locale.ROOT);
        return switch (normalizedSeverity) {
            case "CRITICAL", "HIGH" -> "立即检查电源、传感器和冷却路径。";
            case "MEDIUM" -> "检查设备状态并验证异常指标。";
            default -> "持续监控设备并重新检查规则 " + ruleCode + "。";
        };
    }
}
