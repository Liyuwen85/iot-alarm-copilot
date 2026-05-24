package com.example.iotalarmcopilot.ai.domain.model;

/**
 * AI任务状态
 */
public enum AiSummaryStatus {
    // 待处理
    PENDING,
    // 处理中
    PROCESSING,
    // 处理完成
    SUCCEEDED,
    // 处理失败
    FAILED
}
