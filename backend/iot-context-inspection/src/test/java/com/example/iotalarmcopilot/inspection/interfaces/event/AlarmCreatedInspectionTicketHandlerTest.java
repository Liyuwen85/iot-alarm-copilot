package com.example.iotalarmcopilot.inspection.interfaces.event;

import com.example.iotalarmcopilot.contract.event.AlarmCreatedEvent;
import com.example.iotalarmcopilot.inspection.application.InspectionApplicationService;
import com.example.iotalarmcopilot.inspection.domain.model.InspectionStatus;
import com.example.iotalarmcopilot.inspection.domain.model.InspectionTicket;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketSaveResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlarmCreatedInspectionTicketHandlerTest {

    @Test
    void should_handle_alarm_created_event() {
        InspectionApplicationService service = mock(InspectionApplicationService.class);
        AlarmCreatedInspectionTicketHandler handler = new AlarmCreatedInspectionTicketHandler(service);
        when(service.createIfAbsent(any())).thenReturn(new InspectionTicketSaveResult(new InspectionTicket(
                1L,
                100L,
                "alarm-dedup-key",
                "temperature_high",
                "dev-01",
                "HIGH",
                "Inspection ticket for rule temperature_high on device dev-01",
                "Check power, sensor and cooling paths immediately.",
                InspectionStatus.PENDING,
                Instant.parse("2026-05-18T10:00:00Z"),
                Instant.parse("2026-05-18T10:01:00Z"),
                null,
                null), true));

        handler.onAlarmCreated(new AlarmCreatedEvent(
                1L,
                "alarm-dedup-key",
                "temperature_high",
                "dev-01",
                "HIGH",
                Instant.parse("2026-05-18T10:00:00Z")));

        verify(service).createIfAbsent(any());
    }
}
