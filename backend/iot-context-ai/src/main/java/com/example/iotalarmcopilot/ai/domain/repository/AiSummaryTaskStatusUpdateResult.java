package com.example.iotalarmcopilot.ai.domain.repository;

import com.example.iotalarmcopilot.ai.domain.model.AiSummaryTask;

public record AiSummaryTaskStatusUpdateResult(
        AiSummaryTask task,
        boolean changed) {
}
