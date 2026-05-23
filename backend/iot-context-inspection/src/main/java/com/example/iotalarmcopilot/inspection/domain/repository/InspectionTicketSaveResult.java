package com.example.iotalarmcopilot.inspection.domain.repository;

import com.example.iotalarmcopilot.inspection.domain.model.InspectionTicket;

/**
 * 工单保存结果
 */
public record InspectionTicketSaveResult(
        InspectionTicket ticket,
        boolean created) {
}
