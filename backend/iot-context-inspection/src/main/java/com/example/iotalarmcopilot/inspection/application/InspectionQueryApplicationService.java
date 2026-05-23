package com.example.iotalarmcopilot.inspection.application;

import com.example.iotalarmcopilot.inspection.domain.model.InspectionTicket;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 巡检查询应用服务
 */
@Service
public class InspectionQueryApplicationService {

    private final InspectionTicketRepository inspectionTicketRepository;

    public InspectionQueryApplicationService(InspectionTicketRepository inspectionTicketRepository) {
        this.inspectionTicketRepository = inspectionTicketRepository;
    }

    public InspectionTicketVO get(Long ticketId) {
        return toVO(inspectionTicketRepository.load(ticketId));
    }

    public List<InspectionTicketVO> recent(int limit) {
        return inspectionTicketRepository.recent(limit).stream()
                .map(this::toVO)
                .toList();
    }

    public InspectionTicketVO toVO(InspectionTicket ticket) {
        return new InspectionTicketVO(
                ticket.id(),
                ticket.alarmId(),
                ticket.alarmDedupKey(),
                ticket.ruleCode(),
                ticket.deviceId(),
                ticket.severity(),
                ticket.summary(),
                ticket.suggestion(),
                ticket.status().name(),
                ticket.alarmTriggeredAt(),
                ticket.createdAt(),
                ticket.confirmedAt(),
                ticket.closedAt());
    }
}
