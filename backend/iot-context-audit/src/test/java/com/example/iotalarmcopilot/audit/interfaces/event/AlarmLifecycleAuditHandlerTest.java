package com.example.iotalarmcopilot.audit.interfaces.event;

import com.example.iotalarmcopilot.audit.application.AuditApplicationService;
import com.example.iotalarmcopilot.audit.application.RecordAuditLogCommand;
import com.example.iotalarmcopilot.contract.event.AlarmAcknowledgedEvent;
import com.example.iotalarmcopilot.contract.event.AlarmClosedEvent;
import com.example.iotalarmcopilot.contract.event.AlarmCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AlarmLifecycleAuditHandlerTest {

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    void should_record_created_alarm_audit() throws Exception {
        AuditApplicationService auditApplicationService = mock(AuditApplicationService.class);
        AlarmCreatedAuditHandler handler = new AlarmCreatedAuditHandler(auditApplicationService, objectMapper);
        AlarmCreatedEvent event = new AlarmCreatedEvent(
                1L,
                "temperature_high:dev-01:101",
                "temperature_high",
                "dev-01",
                "WARN",
                Instant.parse("2026-05-13T10:00:00Z"));

        handler.onAlarmCreated(event);

        verify(auditApplicationService).record(new RecordAuditLogCommand(
                "alarm.created",
                "alarm_event",
                "1",
                "dev-01",
                objectMapper.writeValueAsString(event),
                Instant.parse("2026-05-13T10:00:00Z")));
    }

    @Test
    void should_record_acknowledged_alarm_audit() throws Exception {
        AuditApplicationService auditApplicationService = mock(AuditApplicationService.class);
        AlarmAcknowledgedAuditHandler handler = new AlarmAcknowledgedAuditHandler(auditApplicationService, objectMapper);
        AlarmAcknowledgedEvent event = new AlarmAcknowledgedEvent(
                1L,
                "temperature_high:dev-01:101",
                "temperature_high",
                "dev-01",
                "WARN",
                Instant.parse("2026-05-13T10:05:00Z"));

        handler.onAlarmAcknowledged(event);

        verify(auditApplicationService).record(new RecordAuditLogCommand(
                "alarm.acknowledged",
                "alarm_event",
                "1",
                "dev-01",
                objectMapper.writeValueAsString(event),
                Instant.parse("2026-05-13T10:05:00Z")));
    }

    @Test
    void should_record_closed_alarm_audit() throws Exception {
        AuditApplicationService auditApplicationService = mock(AuditApplicationService.class);
        AlarmClosedAuditHandler handler = new AlarmClosedAuditHandler(auditApplicationService, objectMapper);
        AlarmClosedEvent event = new AlarmClosedEvent(
                1L,
                "temperature_high:dev-01:101",
                "temperature_high",
                "dev-01",
                "WARN",
                Instant.parse("2026-05-13T10:06:00Z"));

        handler.onAlarmClosed(event);

        verify(auditApplicationService).record(new RecordAuditLogCommand(
                "alarm.closed",
                "alarm_event",
                "1",
                "dev-01",
                objectMapper.writeValueAsString(event),
                Instant.parse("2026-05-13T10:06:00Z")));
    }
}
