package com.example.iotalarmcopilot.alarm.application;

import com.example.iotalarmcopilot.alarm.domain.Alarm;
import com.example.iotalarmcopilot.alarm.domain.AlarmRepository;
import com.example.iotalarmcopilot.alarm.domain.AlarmSaveResult;
import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.event.AlarmAcknowledgedEvent;
import com.example.iotalarmcopilot.contract.event.AlarmClosedEvent;
import com.example.iotalarmcopilot.contract.event.AlarmCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlarmApplicationServiceTest {

    @Test
    void should_publish_created_event_when_alarm_is_new() {
        AlarmRepository repository = mock(AlarmRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        AlarmApplicationService service = new AlarmApplicationService(repository, publisher);
        Instant triggeredAt = Instant.parse("2026-05-13T10:00:00Z");
        CreateAlarmFromRuleCommand command = new CreateAlarmFromRuleCommand(
                "temperature_high",
                101L,
                "dev-01",
                "temperature",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                triggeredAt);
        Alarm savedAlarm = Alarm.openFromRule(
                "temperature_high",
                101L,
                "dev-01",
                "temperature",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                triggeredAt);
        savedAlarm = new Alarm(
                1L,
                savedAlarm.dedupKey(),
                savedAlarm.ruleCode(),
                savedAlarm.telemetryEventId(),
                savedAlarm.deviceId(),
                savedAlarm.metricName(),
                savedAlarm.metricValue(),
                savedAlarm.threshold(),
                savedAlarm.severity(),
                savedAlarm.status(),
                savedAlarm.triggeredAt(),
                savedAlarm.acknowledgedAt(),
                savedAlarm.closedAt());

        when(repository.saveIfAbsent(any(Alarm.class))).thenReturn(new AlarmSaveResult(savedAlarm, true));

        service.createIfAbsent(command);

        verify(publisher).publishEvent(new AlarmCreatedEvent(
                1L,
                savedAlarm.dedupKey().value(),
                "temperature_high",
                "dev-01",
                savedAlarm.severity().name(),
                triggeredAt));
    }

    @Test
    void should_reject_ack_when_alarm_already_acked() {
        AlarmRepository repository = mock(AlarmRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        AlarmApplicationService service = new AlarmApplicationService(repository, publisher);
        Instant triggeredAt = Instant.parse("2026-05-13T10:00:00Z");
        Instant acknowledgedAt = Instant.parse("2026-05-13T10:05:00Z");
        Alarm ackedAlarm = Alarm.openFromRule(
                "temperature_high",
                101L,
                "dev-01",
                "temperature",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                triggeredAt).acknowledge(acknowledgedAt);
        ackedAlarm = new Alarm(
                1L,
                ackedAlarm.dedupKey(),
                ackedAlarm.ruleCode(),
                ackedAlarm.telemetryEventId(),
                ackedAlarm.deviceId(),
                ackedAlarm.metricName(),
                ackedAlarm.metricValue(),
                ackedAlarm.threshold(),
                ackedAlarm.severity(),
                ackedAlarm.status(),
                ackedAlarm.triggeredAt(),
                ackedAlarm.acknowledgedAt(),
                ackedAlarm.closedAt());

        when(repository.load(1L)).thenReturn(ackedAlarm);

        assertThrows(BaseDomainException.class, () ->
                service.acknowledge(new AcknowledgeAlarmCommand(1L, Instant.parse("2026-05-13T10:06:00Z"))));

        verify(repository, never()).updateStatus(any(Alarm.class));
        verify(publisher, never()).publishEvent(any(AlarmAcknowledgedEvent.class));
    }

    @Test
    void should_publish_closed_event_when_alarm_transitions_to_closed() {
        AlarmRepository repository = mock(AlarmRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        AlarmApplicationService service = new AlarmApplicationService(repository, publisher);
        Instant triggeredAt = Instant.parse("2026-05-13T10:00:00Z");
        Instant closedAt = Instant.parse("2026-05-13T10:06:00Z");
        Alarm openAlarm = Alarm.openFromRule(
                "temperature_high",
                101L,
                "dev-01",
                "temperature",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                triggeredAt);
        openAlarm = new Alarm(
                1L,
                openAlarm.dedupKey(),
                openAlarm.ruleCode(),
                openAlarm.telemetryEventId(),
                openAlarm.deviceId(),
                openAlarm.metricName(),
                openAlarm.metricValue(),
                openAlarm.threshold(),
                openAlarm.severity(),
                openAlarm.status(),
                openAlarm.triggeredAt(),
                openAlarm.acknowledgedAt(),
                openAlarm.closedAt());
        Alarm closedAlarm = openAlarm.close(closedAt);

        when(repository.load(1L)).thenReturn(openAlarm);
        when(repository.updateStatus(any(Alarm.class))).thenReturn(closedAlarm);

        service.close(new CloseAlarmCommand(1L, closedAt));

        verify(publisher).publishEvent(new AlarmClosedEvent(
                1L,
                openAlarm.dedupKey().value(),
                "temperature_high",
                "dev-01",
                openAlarm.severity().name(),
                closedAt));
    }
}
