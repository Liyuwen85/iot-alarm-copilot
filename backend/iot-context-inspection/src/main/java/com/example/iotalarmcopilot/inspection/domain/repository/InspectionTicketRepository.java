package com.example.iotalarmcopilot.inspection.domain.repository;

import com.example.iotalarmcopilot.inspection.domain.model.InspectionStatus;
import com.example.iotalarmcopilot.inspection.domain.model.InspectionTicket;

import java.util.List;
import java.util.Optional;

/**
 * 工单存储接口
 */
public interface InspectionTicketRepository {

    InspectionTicketSaveResult saveIfAbsent(InspectionTicket ticket);

    InspectionTicket load(Long ticketId);

    Optional<InspectionTicket> findByAlarmId(Long alarmId);

    /**
     * 更新工单状态
     *
     * @param ticket                工单
     * @param expectedCurrentStatus 当前状态
     * @return 工单状态更新结果
     */
    InspectionTicketStatusUpdateResult updateStatusIfCurrentStatusMatches(
            InspectionTicket ticket,
            InspectionStatus expectedCurrentStatus);

    List<InspectionTicket> recent(int limit);
}
