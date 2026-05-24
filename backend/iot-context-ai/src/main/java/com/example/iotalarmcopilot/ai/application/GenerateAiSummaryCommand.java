package com.example.iotalarmcopilot.ai.application;

import java.time.Instant;
import java.util.Objects;

public record GenerateAiSummaryCommand(
        Long taskId,
        Instant requestedAt) {

    public GenerateAiSummaryCommand {
        Objects.requireNonNull(taskId, "taskId must not be null");
        Objects.requireNonNull(requestedAt, "requestedAt must not be null");
    }
}
