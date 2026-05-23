package com.example.iotalarmcopilot.inspection.infrastructure.persistence;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.inspection.domain.model.InspectionStatus;
import com.example.iotalarmcopilot.inspection.domain.model.InspectionTicket;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketRepository;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketSaveResult;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketStatusUpdateResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisInspectionTicketRepository implements InspectionTicketRepository {

    private final InspectionTicketMapper inspectionTicketMapper;

    public MybatisInspectionTicketRepository(InspectionTicketMapper inspectionTicketMapper) {
        this.inspectionTicketMapper = inspectionTicketMapper;
    }

    @Override
    public InspectionTicketSaveResult saveIfAbsent(InspectionTicket ticket) {
        InspectionTicketRecord record = InspectionTicketRecord.fromDomain(ticket);
        int insertedRows = inspectionTicketMapper.insertIgnore(record);
        InspectionTicketRecord savedRecord = inspectionTicketMapper.selectByAlarmId(ticket.alarmId());
        if (savedRecord == null) {
            throw new BaseDomainException("Failed to persist or load inspection ticket. alarmId=" + ticket.alarmId());
        }
        return new InspectionTicketSaveResult(savedRecord.toDomain(), insertedRows == 1);
    }

    @Override
    public InspectionTicket load(Long ticketId) {
        InspectionTicketRecord record = inspectionTicketMapper.selectById(ticketId);
        if (record == null) {
            throw new BaseDomainException("Inspection ticket not found. id=" + ticketId);
        }
        return record.toDomain();
    }

    @Override
    public Optional<InspectionTicket> findByAlarmId(Long alarmId) {
        return Optional.ofNullable(inspectionTicketMapper.selectByAlarmId(alarmId))
                .map(InspectionTicketRecord::toDomain);
    }

    @Override
    public InspectionTicketStatusUpdateResult updateStatusIfCurrentStatusMatches(
            InspectionTicket ticket,
            InspectionStatus expectedCurrentStatus) {
        InspectionTicketRecord record = InspectionTicketRecord.fromDomain(ticket);
        int updatedRows = inspectionTicketMapper.updateStatusIfCurrentStatusMatches(record, expectedCurrentStatus.name());
        InspectionTicket latestTicket = load(ticket.id());
        if (updatedRows > 1) {
            throw new BaseDomainException("Unexpected updated rows for inspection ticket status transition. id=" + ticket.id());
        }
        return new InspectionTicketStatusUpdateResult(latestTicket, updatedRows == 1);
    }

    @Override
    public List<InspectionTicket> recent(int limit) {
        return inspectionTicketMapper.selectRecent(limit).stream()
                .map(InspectionTicketRecord::toDomain)
                .toList();
    }
}
