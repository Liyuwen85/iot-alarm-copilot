package com.example.iotalarmcopilot.inspection.application;

import com.example.iotalarmcopilot.inspection.domain.model.InspectionStatus;
import com.example.iotalarmcopilot.inspection.domain.model.InspectionTicket;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketRepository;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketSaveResult;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketStatusUpdateResult;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InspectionApplicationServiceTest {

    @Test
    void should_create_ticket_from_alarm() {
        InspectionTicketRepository repository = mock(InspectionTicketRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        InspectionApplicationService service = new InspectionApplicationService(repository, publisher);
        Instant triggeredAt = Instant.parse("2026-05-18T10:00:00Z");
        Instant createdAt = Instant.parse("2026-05-18T10:01:00Z");

        when(repository.saveIfAbsent(any(InspectionTicket.class))).thenAnswer(invocation -> {
            InspectionTicket ticket = invocation.getArgument(0);
            return new InspectionTicketSaveResult(new InspectionTicket(
                    1L,
                    ticket.alarmId(),
                    ticket.alarmDedupKey(),
                    ticket.ruleCode(),
                    ticket.deviceId(),
                    ticket.severity(),
                    ticket.summary(),
                    ticket.suggestion(),
                    ticket.status(),
                    ticket.alarmTriggeredAt(),
                    ticket.createdAt(),
                    ticket.confirmedAt(),
                    ticket.closedAt()), true);
        });

        InspectionTicketSaveResult result = service.createIfAbsent(new CreateInspectionTicketFromAlarmCommand(
                100L,
                "alarm-dedup-key",
                "temperature_high",
                "dev-01",
                "HIGH",
                triggeredAt,
                createdAt));

        assertEquals(InspectionStatus.PENDING, result.ticket().status());
        assertEquals("dev-01", result.ticket().deviceId());
    }

    @Test
    void should_confirm_ticket() {
        InspectionTicketRepository repository = mock(InspectionTicketRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        InspectionApplicationService service = new InspectionApplicationService(repository, publisher);
        Instant createdAt = Instant.parse("2026-05-18T10:01:00Z");
        Instant confirmedAt = Instant.parse("2026-05-18T10:05:00Z");
        InspectionTicket current = new InspectionTicket(
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
                createdAt,
                null,
                null);
        InspectionTicket confirmed = current.confirm(confirmedAt);

        when(repository.load(1L)).thenReturn(current);
        when(repository.updateStatusIfCurrentStatusMatches(any(InspectionTicket.class), any(InspectionStatus.class)))
                .thenReturn(new InspectionTicketStatusUpdateResult(confirmed, true));

        InspectionTicket result = service.confirm(new ConfirmInspectionTicketCommand(1L, confirmedAt));

        assertEquals(InspectionStatus.CONFIRMED, result.status());
        assertEquals(confirmedAt, result.confirmedAt());
    }

    @Test
    void should_not_publish_confirmed_event_when_ticket_was_changed_by_other_node() {
        InspectionTicketRepository repository = mock(InspectionTicketRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        InspectionApplicationService service = new InspectionApplicationService(repository, publisher);
        Instant createdAt = Instant.parse("2026-05-18T10:01:00Z");
        Instant confirmedAt = Instant.parse("2026-05-18T10:05:00Z");
        Instant closedAt = Instant.parse("2026-05-18T10:06:00Z");
        InspectionTicket current = new InspectionTicket(
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
                createdAt,
                null,
                null);
        InspectionTicket latest = new InspectionTicket(
                1L,
                100L,
                "alarm-dedup-key",
                "temperature_high",
                "dev-01",
                "HIGH",
                "Inspection ticket for rule temperature_high on device dev-01",
                "Check power, sensor and cooling paths immediately.",
                InspectionStatus.CLOSED,
                Instant.parse("2026-05-18T10:00:00Z"),
                createdAt,
                confirmedAt,
                closedAt);

        when(repository.load(1L)).thenReturn(current);
        when(repository.updateStatusIfCurrentStatusMatches(any(InspectionTicket.class), any(InspectionStatus.class)))
                .thenReturn(new InspectionTicketStatusUpdateResult(latest, false));

        InspectionTicket result = service.confirm(new ConfirmInspectionTicketCommand(1L, confirmedAt));

        assertEquals(InspectionStatus.CLOSED, result.status());
        verify(publisher, never()).publishEvent(any());
    }
}
