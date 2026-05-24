package com.example.iotalarmcopilot.ai.domain.policy;

import com.example.iotalarmcopilot.ai.domain.model.AiSummaryTask;

/**
 * 提示词生成
 */
public final class AiPromptTemplatePolicy {

    private AiPromptTemplatePolicy() {
    }

    public static String buildAlarmSummaryPrompt(AiSummaryTask task, String promptVersion) {
        return """
                你是一名面向运维工程师的物联网告警助手。
                请分析告警信息，并仅返回有效的 JSON 格式数据，不要包含 Markdown 代码块标记。
                
                要求的 JSON 结构如下：
                {
                  "summary": "字符串",
                  "possibleCause": "字符串",
                  "inspectionSuggestion": "字符串",
                  "riskLevel": "LOW|MEDIUM|HIGH|CRITICAL",
                  "confidence": 0.0
                }
                
                约束条件：
                - summary: 用一句话简明扼要地总结告警
                - possibleCause: 用一句话简明扼要地说明可能原因
                - inspectionSuggestion: 用一句话列出2-3条可操作的排查建议
                - riskLevel: 必须是 LOW、MEDIUM、HIGH 或 CRITICAL 中的一个
                - confidence: 0 到 1 之间的小数，表示置信度
                - 不要编造不可用的遥测数据细节
                - 主要依据告警严重程度和规则语义进行判断
                
                提示词版本: %s
                
                告警输入:
                {
                  "alarmId": %d,
                  "alarmDedupKey": "%s",
                  "ruleCode": "%s",
                  "deviceId": "%s",
                  "severity": "%s",
                  "alarmTriggeredAt": "%s"
                }
                """.formatted(
                promptVersion,
                task.alarmId(),
                escape(task.alarmDedupKey()),
                escape(task.ruleCode()),
                escape(task.deviceId()),
                escape(task.severity()),
                task.alarmTriggeredAt());
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
