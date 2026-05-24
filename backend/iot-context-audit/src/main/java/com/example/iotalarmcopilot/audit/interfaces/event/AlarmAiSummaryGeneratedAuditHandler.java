package com.example.iotalarmcopilot.audit.interfaces.event;

import com.example.iotalarmcopilot.audit.application.AuditApplicationService;
import com.example.iotalarmcopilot.audit.application.RecordAuditLogCommand;
import com.example.iotalarmcopilot.contract.event.AlarmAiSummaryGeneratedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 告警AI摘要审计处理器
 */
@Slf4j
@Component
public class AlarmAiSummaryGeneratedAuditHandler {

    private final AuditApplicationService auditApplicationService;
    private final ObjectMapper objectMapper;

    public AlarmAiSummaryGeneratedAuditHandler(
            AuditApplicationService auditApplicationService,
            ObjectMapper objectMapper) {
        this.auditApplicationService = auditApplicationService;
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void onAlarmAiSummaryGenerated(AlarmAiSummaryGeneratedEvent event) {
        auditApplicationService.record(new RecordAuditLogCommand(
                event.eventType(),
                "ai_alarm_summary",
                event.summaryTaskId().toString(),
                event.deviceId(),
                writePayload(event),
                event.occurredAt()));
        log.info("Audit recorded for ai summary taskId={}", event.summaryTaskId());
    }

    private String writePayload(AlarmAiSummaryGeneratedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize AI summary audit payload", exception);
        }
    }
}
