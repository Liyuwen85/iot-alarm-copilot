package com.example.iotalarmcopilot.ai.domain.repository;

import com.example.iotalarmcopilot.ai.domain.model.AiSummaryTask;

public record AiSummaryTaskSaveResult(
        AiSummaryTask task,
        boolean created) {
}
