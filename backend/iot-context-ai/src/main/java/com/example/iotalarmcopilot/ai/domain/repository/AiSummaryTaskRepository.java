package com.example.iotalarmcopilot.ai.domain.repository;

import com.example.iotalarmcopilot.ai.domain.model.AiSummaryStatus;
import com.example.iotalarmcopilot.ai.domain.model.AiSummaryTask;

import java.util.List;
import java.util.Optional;

/**
 * AI告警摘要任务仓储接口
 */
public interface AiSummaryTaskRepository {

    /**
     * 没有就新建一个
     *
     * @param task
     * @return
     */
    AiSummaryTaskSaveResult saveIfAbsent(AiSummaryTask task);

    AiSummaryTask load(Long taskId);

    Optional<AiSummaryTask> findByAlarmId(Long alarmId);

    /**
     * 更新状态
     *
     * @param task
     * @param expectedCurrentStatus 当前状态
     * @return
     */
    AiSummaryTaskStatusUpdateResult updateStatusIfCurrentStatusMatches(AiSummaryTask task, AiSummaryStatus expectedCurrentStatus);

    List<AiSummaryTask> recent(int limit);
}
