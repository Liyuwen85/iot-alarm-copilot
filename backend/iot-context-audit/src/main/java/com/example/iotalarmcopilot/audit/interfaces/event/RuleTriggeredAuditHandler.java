package com.example.iotalarmcopilot.audit.interfaces.event;

import com.example.iotalarmcopilot.audit.application.AuditApplicationService;
import com.example.iotalarmcopilot.audit.application.RecordAuditLogCommand;
import com.example.iotalarmcopilot.contract.event.RuleTriggeredEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 规则触发审计处理器
 */
@Slf4j
@Component
public class RuleTriggeredAuditHandler {

    private final AuditApplicationService auditApplicationService;
    private final ObjectMapper objectMapper;

    public RuleTriggeredAuditHandler(
            AuditApplicationService auditApplicationService,
            ObjectMapper objectMapper) {
        this.auditApplicationService = auditApplicationService;
        this.objectMapper = objectMapper;
    }

    /**
     * 监听规则触发事件
     *
     * @param event 规则触发事件
     */
    @EventListener
    public void onRuleTriggered(RuleTriggeredEvent event) {
        auditApplicationService.record(new RecordAuditLogCommand(
                event.eventType(),
                "rule_trigger",
                event.ruleCode() + ":" + event.telemetryEventId(),
                event.deviceId(),
                writePayload(event),
                event.occurredAt()));
        log.info("Audit recorded for ruleCode={}, telemetryEventId={}", event.ruleCode(), event.telemetryEventId());
    }

    private String writePayload(RuleTriggeredEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize rule audit payload", exception);
        }
    }
}
