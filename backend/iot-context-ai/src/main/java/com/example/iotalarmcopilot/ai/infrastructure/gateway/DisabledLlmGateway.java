package com.example.iotalarmcopilot.ai.infrastructure.gateway;

/**
 * 禁用的 LLM 告警摘要网关
 */
public class DisabledLlmGateway implements LlmGateway {

    private final AiProperties aiProperties;

    public DisabledLlmGateway(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public LlmAlarmSummaryResult generateAlarmSummary(LlmAlarmSummaryRequest request) {
        if (!aiProperties.isEnabled()) {
            throw new IllegalStateException("AI summary is disabled. Set iot.ai.enabled=true to enable it.");
        }
        throw new IllegalStateException("Spring AI ChatClient is not available. Check Spring AI dependency and provider configuration.");
    }
}
