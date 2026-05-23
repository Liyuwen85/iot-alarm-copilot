package com.example.iotalarmcopilot.inspection.application;

import com.example.iotalarmcopilot.contract.event.InspectionTicketClosedEvent;
import com.example.iotalarmcopilot.contract.event.InspectionTicketConfirmedEvent;
import com.example.iotalarmcopilot.contract.event.InspectionTicketCreatedEvent;
import com.example.iotalarmcopilot.inspection.domain.model.InspectionStatus;
import com.example.iotalarmcopilot.inspection.domain.model.InspectionTicket;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketRepository;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketSaveResult;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketStatusUpdateResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 巡检工单应用服务
 */
@Service
public class InspectionApplicationService {

    private final InspectionTicketRepository inspectionTicketRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public InspectionApplicationService(
            InspectionTicketRepository inspectionTicketRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.inspectionTicketRepository = inspectionTicketRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public InspectionTicketSaveResult createIfAbsent(CreateInspectionTicketFromAlarmCommand command) {
        InspectionTicket ticket = InspectionTicket.openFromAlarm(
                command.alarmId(),
                command.alarmDedupKey(),
                command.ruleCode(),
                command.deviceId(),
                command.severity(),
                command.alarmTriggeredAt(),
                command.createdAt());
        InspectionTicketSaveResult saveResult = inspectionTicketRepository.saveIfAbsent(ticket);
        if (saveResult.created()) {
            publishCreated(saveResult.ticket());
        }
        return saveResult;
    }

    @Transactional
    public InspectionTicket confirm(ConfirmInspectionTicketCommand command) {
        InspectionTicket currentTicket = inspectionTicketRepository.load(command.ticketId());
        InspectionTicket confirmedTicket = currentTicket.confirm(command.confirmedAt());
        InspectionTicketStatusUpdateResult updateResult = inspectionTicketRepository.updateStatusIfCurrentStatusMatches(
                confirmedTicket,
                currentTicket.status());
        InspectionTicket savedTicket = updateResult.ticket();
        if (updateResult.changed() && savedTicket.status() == InspectionStatus.CONFIRMED) {
            publishConfirmed(savedTicket);
        }
        return savedTicket;
    }

    @Transactional
    public InspectionTicket close(CloseInspectionTicketCommand command) {
        InspectionTicket currentTicket = inspectionTicketRepository.load(command.ticketId());
        InspectionTicket closedTicket = currentTicket.close(command.closedAt());
        InspectionTicketStatusUpdateResult updateResult = inspectionTicketRepository.updateStatusIfCurrentStatusMatches(
                closedTicket,
                currentTicket.status());
        InspectionTicket savedTicket = updateResult.ticket();
        if (updateResult.changed() && savedTicket.status() == InspectionStatus.CLOSED) {
            publishClosed(savedTicket);
        }
        return savedTicket;
    }

    private void publishCreated(InspectionTicket ticket) {
        applicationEventPublisher.publishEvent(new InspectionTicketCreatedEvent(
                ticket.id(),
                ticket.alarmId(),
                ticket.alarmDedupKey(),
                ticket.ruleCode(),
                ticket.deviceId(),
                ticket.severity(),
                ticket.createdAt()));
    }

    private void publishConfirmed(InspectionTicket ticket) {
        applicationEventPublisher.publishEvent(new InspectionTicketConfirmedEvent(
                ticket.id(),
                ticket.alarmId(),
                ticket.alarmDedupKey(),
                ticket.ruleCode(),
                ticket.deviceId(),
                ticket.severity(),
                ticket.confirmedAt()));
    }

    private void publishClosed(InspectionTicket ticket) {
        applicationEventPublisher.publishEvent(new InspectionTicketClosedEvent(
                ticket.id(),
                ticket.alarmId(),
                ticket.alarmDedupKey(),
                ticket.ruleCode(),
                ticket.deviceId(),
                ticket.severity(),
                ticket.closedAt()));
    }
}
