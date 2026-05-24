package com.example.iotalarmcopilot.ai.infrastructure.gateway;

import com.example.iotalarmcopilot.ai.domain.model.AiStructuredSummary;

import java.util.Objects;

/**
 * 结果输出
 */
public record LlmAlarmSummaryResult(
        AiStructuredSummary summary,
        String modelName,
        String promptVersion,
        String rawResponse) {

    public LlmAlarmSummaryResult {
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(modelName, "modelName must not be null");
        Objects.requireNonNull(promptVersion, "promptVersion must not be null");
        Objects.requireNonNull(rawResponse, "rawResponse must not be null");
    }
}
