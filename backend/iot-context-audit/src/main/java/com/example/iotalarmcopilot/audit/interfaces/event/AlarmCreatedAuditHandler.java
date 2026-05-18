package com.example.iotalarmcopilot.audit.interfaces.event;

import com.example.iotalarmcopilot.audit.application.AuditApplicationService;
import com.example.iotalarmcopilot.audit.application.RecordAuditLogCommand;
import com.example.iotalarmcopilot.contract.event.AlarmCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 告警创建审计处理器
 */
@Slf4j
@Component
public class AlarmCreatedAuditHandler {

    private final AuditApplicationService auditApplicationService;
    private final ObjectMapper objectMapper;

    public AlarmCreatedAuditHandler(
            AuditApplicationService auditApplicationService,
            ObjectMapper objectMapper) {
        this.auditApplicationService = auditApplicationService;
        this.objectMapper = objectMapper;
    }

    /**
     * 监听告警创建事件
     *
     * @param event
     */
    @EventListener
    public void onAlarmCreated(AlarmCreatedEvent event) {
        auditApplicationService.record(new RecordAuditLogCommand(
                event.eventType(),
                "alarm_event",
                event.alarmId().toString(),
                event.deviceId(),
                writePayload(event),
                event.occurredAt()));
        log.info("Audit recorded for alarmId={}", event.alarmId());
    }

    private String writePayload(AlarmCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize alarm audit payload", exception);
        }
    }
}
