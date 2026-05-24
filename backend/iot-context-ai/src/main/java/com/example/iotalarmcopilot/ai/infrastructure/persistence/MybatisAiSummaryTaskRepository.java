package com.example.iotalarmcopilot.ai.infrastructure.persistence;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.ai.domain.model.AiSummaryStatus;
import com.example.iotalarmcopilot.ai.domain.model.AiSummaryTask;
import com.example.iotalarmcopilot.ai.domain.repository.AiSummaryTaskRepository;
import com.example.iotalarmcopilot.ai.domain.repository.AiSummaryTaskSaveResult;
import com.example.iotalarmcopilot.ai.domain.repository.AiSummaryTaskStatusUpdateResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI告警摘要任务仓储接口实现
 */
@Repository
public class MybatisAiSummaryTaskRepository implements AiSummaryTaskRepository {

    private final AiSummaryTaskMapper aiSummaryTaskMapper;

    public MybatisAiSummaryTaskRepository(AiSummaryTaskMapper aiSummaryTaskMapper) {
        this.aiSummaryTaskMapper = aiSummaryTaskMapper;
    }

    @Override
    public AiSummaryTaskSaveResult saveIfAbsent(AiSummaryTask task) {
        AiSummaryTaskRecord record = AiSummaryTaskRecord.fromDomain(task);
        // 幂等
        int insertedRows = aiSummaryTaskMapper.insertIgnore(record);
        AiSummaryTaskRecord savedRecord = aiSummaryTaskMapper.selectByAlarmId(task.alarmId());
        if (savedRecord == null) {
            throw new BaseDomainException("Failed to persist or load AI summary task");
        }
        return new AiSummaryTaskSaveResult(savedRecord.toDomain(), insertedRows == 1);
    }

    @Override
    public AiSummaryTask load(Long taskId) {
        AiSummaryTaskRecord record = aiSummaryTaskMapper.selectById(taskId);
        if (record == null) {
            throw new BaseDomainException("AI summary task not found. id=" + taskId);
        }
        return record.toDomain();
    }

    @Override
    public Optional<AiSummaryTask> findByAlarmId(Long alarmId) {
        return Optional.ofNullable(aiSummaryTaskMapper.selectByAlarmId(alarmId))
                .map(AiSummaryTaskRecord::toDomain);
    }

    @Override
    public AiSummaryTaskStatusUpdateResult updateStatusIfCurrentStatusMatches(
            AiSummaryTask task,
            AiSummaryStatus expectedCurrentStatus) {
        AiSummaryTaskRecord record = AiSummaryTaskRecord.fromDomain(task);
        int updatedRows = aiSummaryTaskMapper.updateStatusIfCurrentStatusMatches(record, expectedCurrentStatus.name());
        AiSummaryTask savedTask = load(task.id());
        return new AiSummaryTaskStatusUpdateResult(savedTask, updatedRows == 1);
    }

    @Override
    public List<AiSummaryTask> recent(int limit) {
        return aiSummaryTaskMapper.selectRecent(limit).stream()
                .map(AiSummaryTaskRecord::toDomain)
                .toList();
    }
}
