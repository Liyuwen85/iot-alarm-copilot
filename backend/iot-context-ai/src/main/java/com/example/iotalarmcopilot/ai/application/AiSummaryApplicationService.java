package com.example.iotalarmcopilot.ai.application;

import com.example.iotalarmcopilot.ai.domain.model.AiStructuredSummary;
import com.example.iotalarmcopilot.ai.domain.model.AiSummaryStatus;
import com.example.iotalarmcopilot.ai.domain.model.AiSummaryTask;
import com.example.iotalarmcopilot.ai.domain.policy.AiPromptTemplatePolicy;
import com.example.iotalarmcopilot.ai.domain.repository.AiSummaryTaskRepository;
import com.example.iotalarmcopilot.ai.domain.repository.AiSummaryTaskSaveResult;
import com.example.iotalarmcopilot.ai.domain.repository.AiSummaryTaskStatusUpdateResult;
import com.example.iotalarmcopilot.ai.infrastructure.gateway.AiProperties;
import com.example.iotalarmcopilot.ai.infrastructure.gateway.LlmAlarmSummaryRequest;
import com.example.iotalarmcopilot.ai.infrastructure.gateway.LlmAlarmSummaryResult;
import com.example.iotalarmcopilot.ai.infrastructure.gateway.LlmGateway;
import com.example.iotalarmcopilot.contract.event.AlarmAiSummaryFailedEvent;
import com.example.iotalarmcopilot.contract.event.AlarmAiSummaryGeneratedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;

/**
 * AI告警摘要生成服务
 */
@Service
public class AiSummaryApplicationService {

    private final AiSummaryTaskRepository aiSummaryTaskRepository;
    private final LlmGateway llmGateway;
    private final AiProperties aiProperties;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionTemplate transactionTemplate;

    public AiSummaryApplicationService(
            AiSummaryTaskRepository aiSummaryTaskRepository,
            LlmGateway llmGateway,
            AiProperties aiProperties,
            ApplicationEventPublisher applicationEventPublisher,
            TransactionTemplate transactionTemplate) {
        this.aiSummaryTaskRepository = aiSummaryTaskRepository;
        this.llmGateway = llmGateway;
        this.aiProperties = aiProperties;
        this.applicationEventPublisher = applicationEventPublisher;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * 创建一个待处理的AI告警摘要任务
     *
     * @param command 创建任务命令
     * @return 创建结果
     */
    public AiSummaryTaskSaveResult createPendingIfAbsent(CreateAiSummaryTaskCommand command) {
        // 编程式事务确保数据一致性
        // saveIfAbsent幂等
        return transactionTemplate.execute(status -> aiSummaryTaskRepository.saveIfAbsent(
                AiSummaryTask.createPending(
                        command.alarmId(),
                        command.alarmDedupKey(),
                        command.ruleCode(),
                        command.deviceId(),
                        command.severity(),
                        command.alarmTriggeredAt(),
                        command.createdAt())));
    }

    /**
     * 执行AI告警摘要生成任务
     *
     * @param command 生成命令
     */
    public void generateIfPending(GenerateAiSummaryCommand command) {
        Optional<AiSummaryTask> claimedTask = transactionTemplate.execute(status -> {
            AiSummaryTask currentTask = aiSummaryTaskRepository.load(command.taskId());
            // 认领任务
            AiSummaryTask processingTask = currentTask.claim(command.requestedAt());
            // 更新状态, PENDING -->  PROCESSING
            AiSummaryTaskStatusUpdateResult updateResult = aiSummaryTaskRepository.updateStatusIfCurrentStatusMatches(
                    processingTask,
                    currentTask.status());
            return updateResult.changed() ? Optional.of(updateResult.task()) : Optional.empty();
        });
        if (claimedTask == null || claimedTask.isEmpty()) {
            return;
        }

        AiSummaryTask processingTask = claimedTask.get();
        // 生成提示词
        String prompt = AiPromptTemplatePolicy.buildAlarmSummaryPrompt(processingTask, aiProperties.getPromptVersion());
        try {
            // LLM调用处理
            LlmAlarmSummaryResult llmResult = llmGateway.generateAlarmSummary(new LlmAlarmSummaryRequest(
                    processingTask.alarmId(),
                    processingTask.alarmDedupKey(),
                    processingTask.ruleCode(),
                    processingTask.deviceId(),
                    processingTask.severity(),
                    processingTask.alarmTriggeredAt(),
                    aiProperties.getPromptVersion(),
                    prompt));
            // 成功
            AiStructuredSummary structuredSummary = llmResult.summary();
            AiSummaryTask succeededTask = processingTask.succeed(
                    structuredSummary,
                    llmResult.modelName(),
                    llmResult.promptVersion(),
                    prompt,
                    llmResult.rawResponse(),
                    Instant.now());

            // 持久化
            transactionTemplate.execute(status -> aiSummaryTaskRepository.updateStatusIfCurrentStatusMatches(
                    succeededTask,
                    AiSummaryStatus.PROCESSING));

            // 发布领域事件
            publishGenerated(succeededTask);
        } catch (Exception exception) {
            // 异常失败记录
            AiSummaryTask failedTask = processingTask.fail(
                    aiProperties.getModel(),
                    aiProperties.getPromptVersion(),
                    prompt,
                    null,
                    "AI_SUMMARY_FAILED",
                    exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage(),
                    Instant.now());
            transactionTemplate.execute(status -> aiSummaryTaskRepository.updateStatusIfCurrentStatusMatches(
                    failedTask,
                    AiSummaryStatus.PROCESSING));
            // 发布失败事件
            publishFailed(failedTask);
        }
    }

    private void publishGenerated(AiSummaryTask task) {
        applicationEventPublisher.publishEvent(new AlarmAiSummaryGeneratedEvent(
                task.id(),
                task.alarmId(),
                task.alarmDedupKey(),
                task.ruleCode(),
                task.deviceId(),
                task.severity(),
                task.riskLevel(),
                task.confidence(),
                task.modelName(),
                task.promptVersion(),
                task.finishedAt()));
    }

    private void publishFailed(AiSummaryTask task) {
        applicationEventPublisher.publishEvent(new AlarmAiSummaryFailedEvent(
                task.id(),
                task.alarmId(),
                task.alarmDedupKey(),
                task.ruleCode(),
                task.deviceId(),
                task.severity(),
                task.errorCode(),
                task.errorMessage(),
                task.finishedAt()));
    }
}
