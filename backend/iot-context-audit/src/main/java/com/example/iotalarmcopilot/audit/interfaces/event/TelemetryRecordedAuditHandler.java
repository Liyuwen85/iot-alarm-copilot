package com.example.iotalarmcopilot.audit.interfaces.event;

import com.example.iotalarmcopilot.audit.application.AuditApplicationService;
import com.example.iotalarmcopilot.audit.application.RecordAuditLogCommand;
import com.example.iotalarmcopilot.contract.event.TelemetryRecordedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 遥测记录审计处理器
 */
@Slf4j
@Component
public class TelemetryRecordedAuditHandler {

    private final AuditApplicationService auditApplicationService;
    private final ObjectMapper objectMapper;

    public TelemetryRecordedAuditHandler(
            AuditApplicationService auditApplicationService,
            ObjectMapper objectMapper) {
        this.auditApplicationService = auditApplicationService;
        this.objectMapper = objectMapper;
    }

    /**
     * 监听遥测记录事件
     *
     * @param event
     */
    @EventListener
    public void onTelemetryRecorded(TelemetryRecordedEvent event) {
        auditApplicationService.record(new RecordAuditLogCommand(
                event.eventType(),
                "telemetry_event",
                event.telemetryEventId().toString(),
                event.deviceId(),
                writePayload(event),
                event.occurredAt()));
        log.info("Audit recorded for telemetryEventId={}", event.telemetryEventId());
    }

    private String writePayload(TelemetryRecordedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize telemetry audit payload", exception);
        }
    }
}
