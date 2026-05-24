package com.example.iotalarmcopilot.ai.application;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.ai.domain.model.AiSummaryTask;
import com.example.iotalarmcopilot.ai.domain.repository.AiSummaryTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI告警摘要查询服务
 */
@Service
public class AiSummaryQueryApplicationService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final AiSummaryTaskRepository aiSummaryTaskRepository;

    public AiSummaryQueryApplicationService(AiSummaryTaskRepository aiSummaryTaskRepository) {
        this.aiSummaryTaskRepository = aiSummaryTaskRepository;
    }

    public AiAlarmSummaryVO getByAlarmId(Long alarmId) {
        return aiSummaryTaskRepository.findByAlarmId(alarmId)
                .map(this::toVO)
                .orElseThrow(() -> new BaseDomainException("AI summary task not found. alarmId=" + alarmId));
    }

    public List<AiAlarmSummaryVO> recent(int limit) {
        int safeLimit = normalizeLimit(limit);
        return aiSummaryTaskRepository.recent(safeLimit).stream()
                .map(this::toVO)
                .toList();
    }

    public AiAlarmSummaryVO toVO(AiSummaryTask task) {
        return new AiAlarmSummaryVO(
                task.id(),
                task.alarmId(),
                task.alarmDedupKey(),
                task.ruleCode(),
                task.deviceId(),
                task.severity(),
                task.status().name(),
                task.attemptCount(),
                task.summary(),
                task.possibleCause(),
                task.inspectionSuggestion(),
                task.riskLevel(),
                task.confidence(),
                task.modelName(),
                task.promptVersion(),
                task.errorCode(),
                task.errorMessage(),
                task.alarmTriggeredAt(),
                task.createdAt(),
                task.updatedAt(),
                task.startedAt(),
                task.finishedAt());
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
