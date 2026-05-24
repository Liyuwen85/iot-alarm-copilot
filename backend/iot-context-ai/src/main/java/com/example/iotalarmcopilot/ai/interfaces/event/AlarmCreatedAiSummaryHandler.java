package com.example.iotalarmcopilot.ai.interfaces.event;

import com.example.iotalarmcopilot.ai.application.AiSummaryApplicationService;
import com.example.iotalarmcopilot.ai.application.CreateAiSummaryTaskCommand;
import com.example.iotalarmcopilot.ai.application.GenerateAiSummaryCommand;
import com.example.iotalarmcopilot.ai.domain.repository.AiSummaryTaskSaveResult;
import com.example.iotalarmcopilot.ai.infrastructure.gateway.AiProperties;
import com.example.iotalarmcopilot.contract.event.AlarmCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

/**
 * 监听告警创建事件
 */
@Slf4j
@Component
public class AlarmCreatedAiSummaryHandler {

    private final AiSummaryApplicationService aiSummaryApplicationService;
    private final AiProperties aiProperties;

    public AlarmCreatedAiSummaryHandler(
            AiSummaryApplicationService aiSummaryApplicationService,
            AiProperties aiProperties) {
        this.aiSummaryApplicationService = aiSummaryApplicationService;
        this.aiProperties = aiProperties;
    }

    /**
     * 创建告警时，创建AI摘要任务
     */
    @Async("aiSummaryExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAlarmCreated(AlarmCreatedEvent event) {
        if (!aiProperties.isEnabled()) {
            log.debug("AI summary is disabled, skip alarmId={}", event.alarmId());
            return;
        }
        // 创建任务
        AiSummaryTaskSaveResult saveResult = aiSummaryApplicationService.createPendingIfAbsent(
                new CreateAiSummaryTaskCommand(
                        event.alarmId(),
                        event.dedupKey(),
                        event.ruleCode(),
                        event.deviceId(),
                        event.severity(),
                        event.triggeredAt(),
                        Instant.now()));
        if (!saveResult.created()) {
            log.info("AI summary task deduplicated. taskId={}, alarmId={}", saveResult.task().id(), event.alarmId());
            return;
        }
        log.info("AI summary task created. taskId={}, alarmId={}", saveResult.task().id(), event.alarmId());
        // 执行任务
        aiSummaryApplicationService.generateIfPending(new GenerateAiSummaryCommand(saveResult.task().id(), Instant.now()));
    }
}
