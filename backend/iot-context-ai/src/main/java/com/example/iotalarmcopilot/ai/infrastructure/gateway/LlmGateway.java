package com.example.iotalarmcopilot.ai.infrastructure.gateway;

/**
 * LLM调用网关
 */
public interface LlmGateway {

    /**
     * 生成告警摘要
     *
     * @param request
     * @return
     */
    LlmAlarmSummaryResult generateAlarmSummary(LlmAlarmSummaryRequest request);
}
